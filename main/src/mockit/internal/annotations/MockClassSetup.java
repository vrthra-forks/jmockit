/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.util.*;

import mockit.*;
import mockit.external.asm4.*;
import mockit.internal.*;
import mockit.internal.filtering.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class MockClassSetup
{
   private Class<?> realClass;
   private final Class<?> mockClass;
   private ClassReader rcReader;
   private final AnnotatedMockMethods mockMethods;
   private MockingConfiguration mockingConfiguration;
   private final Object mock;
   private boolean forStartupMock;

   public MockClassSetup(Class<?> mockClass, MockClass metadata)
   {
      this(metadata.realClass(), null, mockClass, metadata);
   }

   private MockClassSetup(Class<?> realClass, Object mock, Class<?> mockClass, MockClass metadata)
   {
      this.realClass = realClass;
      validateRealClass();

      mockMethods = new AnnotatedMockMethods(realClass, null);
      this.mock = mock;
      this.mockClass = mockClass;
      mockingConfiguration = metadata == null ? null : createMockingConfiguration(metadata);

      new AnnotatedMockMethodCollector(mockMethods).collectMockMethods(mockClass);
   }

   private void validateRealClass()
   {
      if (realClass.isAnnotationPresent(MockClass.class)) {
         throw new IllegalArgumentException("Invalid use of mock " + realClass + " where real class was expected");
      }
   }

   private MockingConfiguration createMockingConfiguration(MockClass metadata)
   {
      String[] filters = metadata.stubs();
      return filters.length == 0 ? null : new MockingConfiguration(filters, !metadata.inverse());
   }

   public MockClassSetup(Class<?> realClass, Object mock, Class<?> mockClass)
   {
      this(realClass, mock, mockClass, mockClass.getAnnotation(MockClass.class));
   }

   public MockClassSetup(Object mock, Class<?> mockClass)
   {
      this(getRealClass(mockClass), mock, mockClass);
   }

   public static <T> Class<T> getRealClass(Class<?> specifiedMockClass)
   {
      MockClass mockClassAnnotation = specifiedMockClass.getAnnotation(MockClass.class);

      if (mockClassAnnotation == null) {
         throw new IllegalArgumentException("Missing @MockClass for " + specifiedMockClass);
      }

      @SuppressWarnings("unchecked")
      Class<T> realClass = (Class<T>) mockClassAnnotation.realClass();

      if (realClass == null) {
         // This happens only with the IBM JDK.
         throw new TypeNotPresentException("specified in mock " + specifiedMockClass, null);
      }

      return realClass;
   }

   public MockClassSetup(Class<?> mockClass)
   {
      this(getRealClass(mockClass), null, mockClass);
   }

   public MockClassSetup(Object mockUp)
   {
      Class<?> mockUpClass = mockUp.getClass();
      Type typeToMock = getTypeToMock(mockUpClass);
      ParameterizedType mockedType;

      if (typeToMock instanceof Class<?>) {
         mockedType = null;
         realClass = (Class<?>) typeToMock;
      }
      else if (typeToMock instanceof ParameterizedType){
         mockedType = (ParameterizedType) typeToMock;
         realClass = (Class<?>) mockedType.getRawType();
      }
      else {
         throw new IllegalArgumentException(
            "Invalid target type specified in mock-up " + mockUpClass + ": " + typeToMock);
      }

      validateRealClass();

      mockMethods = new AnnotatedMockMethods(realClass, mockedType);
      mock = mockUp;
      mockClass = mockUpClass;

      new AnnotatedMockMethodCollector(mockMethods).collectMockMethods(mockClass);
   }

   public static Type getTypeToMock(Class<?> mockUpClass)
   {
      Class<?> currentClass = mockUpClass;

      do {
         Type superclass = currentClass.getGenericSuperclass();

         if (superclass instanceof ParameterizedType) {
            return ((ParameterizedType) superclass).getActualTypeArguments()[0];
         }
         else if (superclass == MockUp.class) {
            throw new IllegalArgumentException("No type to be mocked");
         }

         currentClass = (Class<?>) superclass;
      }
      while (true);
   }

   public MockClassSetup(Class<?> realClass, ParameterizedType mockedType, Object mockUp, byte[] realClassCode)
   {
      this.realClass = realClass;
      validateRealClass();

      mockMethods = new AnnotatedMockMethods(realClass, mockedType);
      mock = mockUp;
      mockClass = mockUp.getClass();
      rcReader = realClassCode == null ? null : new ClassReader(realClassCode);

      new AnnotatedMockMethodCollector(mockMethods).collectMockMethods(mockClass);
   }

   public void setUpStartupMock()
   {
      if (realClass != null) {
         forStartupMock = true;
         redefineMethods();
      }
   }

   public Set<Class<?>> redefineMethods()
   {
      Set<Class<?>> redefinedClasses = redefineMethodsInClassHierarchy();
      validateThatAllMockMethodsWereApplied();
      return redefinedClasses;
   }

   private Set<Class<?>> redefineMethodsInClassHierarchy()
   {
      Set<Class<?>> redefinedClasses = new HashSet<Class<?>>();

      while (realClass != null && (mockingConfiguration != null || mockMethods.hasUnusedMocks())) {
         byte[] modifiedClassFile = modifyRealClass();

         if (modifiedClassFile != null) {
            applyClassModifications(modifiedClassFile);
            redefinedClasses.add(realClass);
         }

         Class<?> superClass = realClass.getSuperclass();
         realClass = superClass == Object.class || superClass == Proxy.class ? null : superClass;
         rcReader = null;
         mockingConfiguration = null;
      }

      return redefinedClasses;
   }

   private byte[] modifyRealClass()
   {
      if (rcReader == null) {
         rcReader = createClassReaderForRealClass();
      }

      MockupsModifier modifier =
         new MockupsModifier(rcReader, realClass, mock, mockMethods, mockingConfiguration, forStartupMock);
      rcReader.accept(modifier, 0);

      return modifier.wasModified() ? modifier.toByteArray() : null;
   }

   private ClassReader createClassReaderForRealClass()
   {
      if (realClass.isInterface() || realClass.isArray()) {
         throw new IllegalArgumentException("Not a modifiable class: " + realClass.getName());
      }

      return ClassFile.createReaderFromLastRedefinitionIfAny(realClass);
   }

   private void applyClassModifications(byte[] modifiedClassFile)
   {
      Startup.redefineMethods(realClass, modifiedClassFile);
      mockMethods.registerMockStates();

      if (forStartupMock) {
         CachedClassfiles.addClassfile(realClass, modifiedClassFile);
      }
      else {
         TestRun.mockFixture().addRedefinedClass(mockMethods.getMockClassInternalName(), realClass, modifiedClassFile);
      }
   }

   private void validateThatAllMockMethodsWereApplied()
   {
      List<String> remainingMocks = mockMethods.getUnusedMockSignatures();

      if (!remainingMocks.isEmpty()) {
         String classDesc = mockMethods.getMockClassInternalName();
         String mockSignatures = new MethodFormatter(classDesc).friendlyMethodSignatures(remainingMocks);

         throw new IllegalArgumentException(
            "Matching real methods not found for the following mocks:\n" + mockSignatures);
      }
   }
}
