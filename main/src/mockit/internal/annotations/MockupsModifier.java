/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.lang.reflect.*;

import static java.lang.reflect.Modifier.*;
import static mockit.external.asm4.Opcodes.*;

import mockit.external.asm4.*;
import mockit.external.asm4.Type;
import mockit.internal.*;
import mockit.internal.filtering.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;

/**
 * Responsible for generating all necessary bytecode in the redefined (real) class.
 * Such code will redirect calls made on "real" methods to equivalent calls on the corresponding "mock" methods.
 * The original code won't be executed by the running JVM until the class redefinition is undone.
 * <p/>
 * Methods in the real class which have no corresponding mock method are unaffected.
 * <p/>
 * Any fields (static or not) in the real class remain untouched.
 */
@SuppressWarnings("ClassWithTooManyFields")
final class MockupsModifier extends BaseClassModifier
{
   private static final int IGNORED_ACCESS = ABSTRACT + NATIVE;
   private static final String CLASS_WITH_STATE = "mockit/internal/state/TestRun";

   private final String itFieldDesc;
   private final int mockInstanceIndex;
   private final boolean forStartupMock;
   private final AnnotatedMockMethods annotatedMocks;
   private final MockingConfiguration mockingCfg;

   private final boolean useMockingBridgeForUpdatingMockState;
   private Class<?> mockedClass;

   // Helper fields:
   private AnnotatedMockMethods.MockMethod mockMethod;
   private int varIndex;

   // Output data:
   private boolean classWasModified;

   /**
    * Initializes the modifier for a given real/mock class pair.
    * <p/>
    * If a mock instance is provided, it will receive calls for any instance methods defined in the mock class.
    * If not, a new instance will be created for such calls. In the first case, the mock instance will need to be
    * recovered by the modified bytecode inside the real method. To enable this, the given mock instance is added to the
    * end of a global list made available through {@link mockit.internal.state.TestRun#getMock(int)}.
    *
    * @param cr the class file reader for the real class
    * @param mock an instance of the mock class or null to create one
    * @param mockMethods contains the set of mock methods collected from the mock class; each mock method is identified
    * by a pair composed of "name" and "desc", where "name" is the method name, and "desc" is the JVM internal
    * description of the parameters; once the real class modification is complete this set will be empty, unless no
    * corresponding real method was found for any of its method identifiers
    *
    * @throws IllegalArgumentException if no mock instance is given but the mock class is an inner class, which cannot
    * be instantiated since the enclosing instance is not known
    */
   MockupsModifier(
      ClassReader cr, Class<?> realClass, Object mock,
      AnnotatedMockMethods mockMethods, MockingConfiguration mockingConfiguration, boolean forStartupMock)
   {
      this(
         cr, getItFieldDescriptor(realClass, mockMethods), mockMethods, mockingConfiguration, forStartupMock, mock,
         realClass.getClassLoader() == null);
      inferUseOfMockingBridge(realClass.getClassLoader(), mock);
      mockedClass = realClass;
   }

   private static String getItFieldDescriptor(Class<?> realClass, AnnotatedMockMethods mockMethods)
   {
      if (!mockMethods.supportsItField(realClass)) {
         return null;
      }

      if (Proxy.isProxyClass(realClass)) {
         //noinspection AssignmentToMethodParameter
         realClass = realClass.getInterfaces()[0];
      }

      return Type.getDescriptor(realClass);
   }

   private void inferUseOfMockingBridge(ClassLoader classLoaderOfRealClass, Object mock)
   {
      setUseMockingBridge(classLoaderOfRealClass);

      if (!useMockingBridge && mock != null && !isPublic(mock.getClass().getModifiers())) {
         useMockingBridge = true;
      }
   }

   private MockupsModifier(
      ClassReader cr, String itFieldDesc, AnnotatedMockMethods mockMethods, MockingConfiguration mockingConfiguration,
      boolean forStartupMock, Object mock, boolean useMockingBridgeForUpdatingMockState)
   {
      super(cr);
      this.itFieldDesc = itFieldDesc;
      annotatedMocks = mockMethods;
      mockingCfg = mockingConfiguration;
      this.forStartupMock = forStartupMock;
      mockInstanceIndex = getMockInstanceIndex(mock);
      this.useMockingBridgeForUpdatingMockState = useMockingBridgeForUpdatingMockState;
   }

   private int getMockInstanceIndex(Object mock)
   {
      if (mock != null) {
         return TestRun.getMockClasses().getMocks(forStartupMock).addMock(mock);
      }
      else if (!annotatedMocks.isInnerMockClass()) {
         return -1;
      }

      throw new IllegalArgumentException(
         "An inner mock class cannot be instantiated without its enclosing instance; " +
         "you must either pass a mock instance, or make the class static");
   }

   /**
    * If the specified method has a mock definition, then generates bytecode to redirect calls made to it to the mock
    * method. If it has no mock, does nothing.
    *
    * @param access not relevant
    * @param name together with desc, used to identity the method in given set of mock methods
    * @param signature not relevant
    * @param exceptions not relevant
    *
    * @return null if the method was redefined, otherwise a MethodWriter that writes out the visited method code without
    * changes
    */
   @Override
   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
   {
      if ((access & ACC_SYNTHETIC) != 0) {
         return super.visitMethod(access, name, desc, signature, exceptions);
      }

      if (!hasMock(name, desc, signature)) {
         if (shouldCopyOriginalMethodBytecode(access, name, desc, signature, exceptions)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
         }

         return methodAnnotationsVisitor;
      }

      validateMethodModifiers(access, name);

      startModifiedMethodVersion(access, name, desc, signature, exceptions);
      classWasModified = true;

      if (mockMethod.isForConstructor()) {
         generateCallToSuperConstructor();
      }

      if (isToPreserveRealImplementation(access)) {
         return getAlternativeMethodWriter(access);
      }

      generateCallToUpdateMockStateIfAny(access);
      generateCallToMockMethod(access);
      generateMethodReturn();
      mw.visitMaxs(1, 0); // dummy values, real ones are calculated by ASM
      return methodAnnotationsVisitor;
   }

   private boolean hasMock(String name, String desc, String signature)
   {
      String mockName = getCorrespondingMockName(name);
      mockMethod = annotatedMocks.containsMethod(mockName, desc, signature);
      return mockMethod != null;
   }

   private String getCorrespondingMockName(String name)
   {
      if ("<init>".equals(name)) {
         return "$init";
      }
      else if ("<clinit>".equals(name)) {
         return "$clinit";
      }

      return name;
   }

   private boolean shouldCopyOriginalMethodBytecode(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      if ((access & IGNORED_ACCESS) == 0 && mockingCfg != null && mockingCfg.matchesFilters(name, desc)) {
         startModifiedMethodVersion(access, name, desc, signature, exceptions);
         generateEmptyStubImplementation(name, desc);
         classWasModified = true;
         return false;
      }

      return true;
   }

   private void generateEmptyStubImplementation(String name, String desc)
   {
      if ("<init>".equals(name)) {
         generateCallToSuperConstructor();
      }

      generateEmptyImplementation(desc);
   }

   private void validateMethodModifiers(int access, String name)
   {
      if (isAbstract(access)) {
         throw new IllegalArgumentException("Attempted to mock abstract method \"" + name + '\"');
      }
      else if (isNative(access)) {
         if (!Startup.isJava6OrLater()) {
            throw new IllegalArgumentException(
               "Mocking of native methods not supported under JDK 1.5: \"" + name + '\"');
         }
         else if (mockMethod.isReentrant()) {
            throw new IllegalArgumentException(
               "Reentrant mocks for native methods are not supported: \"" + name + '\"');
         }
      }
   }

   private MethodVisitor getAlternativeMethodWriter(int mockedAccess)
   {
      generateDynamicCallToMock(mockedAccess);

      final boolean forConstructor = methodName.charAt(0) == '<';

      return new MethodVisitor(mw) {
         @Override
         public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
         {
            // Discards debug info with missing information, to avoid a ClassFormatError (happens with EMMA).
            if (end.position > 0) {
               mw.visitLocalVariable(name, desc, signature, start, end, index);
            }
         }

         @Override
         public void visitMethodInsn(int opcode, String owner, String name, String desc)
         {
            if (forConstructor) {
               disregardIfInvokingAnotherConstructor(opcode, owner, name, desc);
            }
            else {
               mw.visitMethodInsn(opcode, owner, name, desc);
            }
         }
      };
   }

   private boolean isToPreserveRealImplementation(int mockedAccess)
   {
      return !isNative(mockedAccess) && (isMockedSuperclass() || mockMethod.isDynamic());
   }

   private boolean isMockedSuperclass()
   {
      return mockedClass != null && mockedClass != mockMethod.getRealClass();
   }

   private void generateDynamicCallToMock(int mockedAccess)
   {
      if (!isStatic(mockedAccess) && !mockMethod.isForConstructor() && isMockedSuperclass()) {
         startOfRealImplementation = new Label();
         mw.visitVarInsn(ALOAD, 0);
         mw.visitTypeInsn(INSTANCEOF, Type.getInternalName(mockMethod.getRealClass()));
         mw.visitJumpInsn(IFEQ, startOfRealImplementation);
      }

      generateCallToUpdateMockStateIfAny(mockedAccess);

      if (mockMethod.isReentrant()) {
         generateCallToReentrantMockMethod(mockedAccess);
      }
      else if (mockMethod.isDynamic()) {
         generateCallToMockMethod(mockedAccess);
         generateDecisionBetweenReturningOrContinuingToRealImplementation();
      }
      else if (startOfRealImplementation != null) {
         generateCallToMockMethod(mockedAccess);
         generateMethodReturn();
         mw.visitLabel(startOfRealImplementation);
      }

      startOfRealImplementation = null;
   }

   private void generateCallToUpdateMockStateIfAny(int mockedAccess)
   {
      int mockStateIndex = mockMethod.getIndexForMockState();

      if (mockStateIndex >= 0) {
         if (useMockingBridgeForUpdatingMockState) {
            generateCallToControlMethodThroughMockingBridge(true, mockedAccess);
            mw.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mw.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
         }
         else {
            mw.visitLdcInsn(annotatedMocks.getMockClassInternalName());
            mw.visitIntInsn(SIPUSH, mockStateIndex);
            mw.visitMethodInsn(INVOKESTATIC, CLASS_WITH_STATE, "updateMockState", "(Ljava/lang/String;I)Z");
         }
      }
   }

   private void generateCallToReentrantMockMethod(int mockedAccess)
   {
      if (startOfRealImplementation == null) {
         startOfRealImplementation = new Label();
      }

      mw.visitJumpInsn(IFEQ, startOfRealImplementation);

      Label l0 = new Label();
      Label l1 = new Label();
      Label l2 = new Label();
      mw.visitTryCatchBlock(l0, l1, l2, null);

      Label l3 = new Label();
      mw.visitTryCatchBlock(l2, l3, l2, null);

      mw.visitLabel(l0);

      generateCallToMockMethod(mockedAccess);

      mw.visitLabel(l1);
      generateCallToExitReentrantMock();
      generateMethodReturn();
      mw.visitLabel(l2);
      mw.visitVarInsn(ASTORE, varIndex);
      mw.visitLabel(l3);
      generateCallToExitReentrantMock();
      mw.visitVarInsn(ALOAD, varIndex);
      mw.visitInsn(ATHROW);

      mw.visitLabel(startOfRealImplementation);
   }

   private void generateCallToControlMethodThroughMockingBridge(boolean enteringMethod, int mockAccess)
   {
      generateCodeToObtainInstanceOfMockingBridge(MockupBridge.MB);

      // First and second "invoke" arguments:
      generateCodeToPassThisOrNullIfStaticMethod(mockAccess);
      mw.visitInsn(ACONST_NULL);

      // Create array for call arguments (third "invoke" argument):
      generateCodeToCreateArrayOfObject(3);

      int i = 0;
      generateCodeToFillArrayElement(i++, enteringMethod);
      generateCodeToFillArrayElement(i++, annotatedMocks.getMockClassInternalName());
      generateCodeToFillArrayElement(i, mockMethod.getIndexForMockState());

      generateCallToInvocationHandler();
   }

   private void generateCallToMockMethod(int access)
   {
      if (mockMethod.isStatic) {
         generateStaticMethodCall(access);
      }
      else {
         generateInstanceMethodCall(access);
      }
   }

   private void generateStaticMethodCall(int access)
   {
      if (shouldUseMockingBridge()) {
         generateCallToMockMethodThroughMockingBridge(false, access);
      }
      else {
         generateMethodOrConstructorArguments(access);
         mw.visitMethodInsn(INVOKESTATIC, annotatedMocks.getMockClassInternalName(), mockMethod.name, mockMethod.desc);
      }
   }

   private boolean shouldUseMockingBridge() { return useMockingBridge || mockMethod.hasInvocationParameter; }

   private void generateCallToMockMethodThroughMockingBridge(boolean callingInstanceMethod, int access)
   {
      generateCodeToObtainInstanceOfMockingBridge(MockMethodBridge.MB);

      // First and second "invoke" arguments:
      boolean isStatic = generateCodeToPassThisOrNullIfStaticMethod(access);
      mw.visitInsn(ACONST_NULL);

      // Create array for call arguments (third "invoke" argument):
      Type[] argTypes = Type.getArgumentTypes(methodDesc);
      generateCodeToCreateArrayOfObject(7 + argTypes.length);

      int i = 0;
      generateCodeToFillArrayElement(i++, callingInstanceMethod);
      generateCodeToFillArrayElement(i++, annotatedMocks.getMockClassInternalName());
      generateCodeToFillArrayElement(i++, mockMethod.name);
      generateCodeToFillArrayElement(i++, mockMethod.desc);
      generateCodeToFillArrayElement(i++, mockMethod.getIndexForMockState());
      generateCodeToFillArrayElement(i++, mockInstanceIndex);
      generateCodeToFillArrayElement(i++, forStartupMock);

      generateCodeToPassMethodArgumentsAsVarargs(argTypes, i, isStatic ? 0 : 1);
      generateCallToInvocationHandler();
   }

   private void generateInstanceMethodCall(int access)
   {
      if (shouldUseMockingBridge()) {
         generateCallToMockMethodThroughMockingBridge(true, access);
         return;
      }

      if (mockInstanceIndex < 0) {
         // No mock instance available yet.
         generateMockObjectInstantiation();
      }
      else {
         // A mock instance is available, so retrieve it from the global list.
         generateGetMockCallWithMockInstanceIndex();
      }

      if (!isStatic(access) && itFieldDesc != null) {
         generateItFieldSetting();
      }

      generateMockInstanceMethodInvocationWithRealMethodArgs(access);
   }

   private void generateMockObjectInstantiation()
   {
      String classInternalName = annotatedMocks.getMockClassInternalName();
      mw.visitTypeInsn(NEW, classInternalName);
      mw.visitInsn(DUP);
      mw.visitMethodInsn(INVOKESPECIAL, classInternalName, "<init>", "()V");
   }

   private void generateGetMockCallWithMockInstanceIndex()
   {
      mw.visitIntInsn(SIPUSH, mockInstanceIndex);
      String getterName = forStartupMock ? "getStartupMock" : "getMock";
      mw.visitMethodInsn(INVOKESTATIC, "mockit/internal/state/TestRun", getterName, "(I)Ljava/lang/Object;");
      mw.visitTypeInsn(CHECKCAST, annotatedMocks.getMockClassInternalName());
   }

   private void generateItFieldSetting()
   {
      Type[] argTypes = Type.getArgumentTypes(mockMethod.desc);
      int var = 1;

      for (Type argType : argTypes) {
         var += argType.getSize();
      }

      mw.visitVarInsn(ASTORE, var); // stores the mock instance into local variable
      mw.visitVarInsn(ALOAD, var);  // loads the mock instance onto the operand stack
      mw.visitVarInsn(ALOAD, 0); // loads "this" onto the operand stack
      mw.visitFieldInsn(PUTFIELD, annotatedMocks.getMockClassInternalName(), "it", itFieldDesc);
      mw.visitVarInsn(ALOAD, var);  // again loads the mock instance onto the stack
   }

   private void generateMockInstanceMethodInvocationWithRealMethodArgs(int access)
   {
      generateMethodOrConstructorArguments(access);
      mw.visitMethodInsn(INVOKEVIRTUAL, annotatedMocks.getMockClassInternalName(), mockMethod.name, mockMethod.desc);
   }

   private void generateMethodOrConstructorArguments(int access)
   {
      boolean hasInvokedInstance = (access & ACC_STATIC) == 0;
      varIndex = hasInvokedInstance ? 1 : 0;

      Type[] argTypes = Type.getArgumentTypes(mockMethod.desc);
      boolean forGenericMethod = mockMethod.isForGenericMethod();

      for (Type argType : argTypes) {
         int opcode = argType.getOpcode(ILOAD);
         mw.visitVarInsn(opcode, varIndex);

         if (forGenericMethod && argType.getSort() >= Type.ARRAY) {
            mw.visitTypeInsn(CHECKCAST, argType.getInternalName());
         }

         varIndex += argType.getSize();
      }
   }

   private void generateMethodReturn()
   {
      if (shouldUseMockingBridge()) {
         generateReturnWithObjectAtTopOfTheStack(methodDesc);
      }
      else {
         Type returnType = Type.getReturnType(methodDesc);
         mw.visitInsn(returnType.getOpcode(IRETURN));
      }
   }

   private void generateCallToExitReentrantMock()
   {
      if (useMockingBridgeForUpdatingMockState) {
         generateCallToControlMethodThroughMockingBridge(false, ACC_STATIC);
         mw.visitInsn(POP);
      }
      else {
         mw.visitLdcInsn(annotatedMocks.getMockClassInternalName());
         mw.visitIntInsn(SIPUSH, mockMethod.getIndexForMockState());
         mw.visitMethodInsn(INVOKESTATIC, CLASS_WITH_STATE, "exitReentrantMock", "(Ljava/lang/String;I)V");
      }
   }

   boolean wasModified() { return classWasModified; }
}
