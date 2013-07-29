/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;
import java.util.*;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;

import static mockit.Deencapsulation.*;
import static mockit.Mockit.*;
import static org.junit.Assert.*;
import org.junit.*;

import mockit.internal.*;

@SuppressWarnings({"JUnitTestMethodWithNoAssertions"})
public final class MockAnnotationsTest
{
   // The "code under test" for the tests in this class ///////////////////////////////////////////////////////////////

   private final CodeUnderTest codeUnderTest = new CodeUnderTest();
   private boolean mockExecuted;

   static class CodeUnderTest
   {
      private final Collaborator dependency = new Collaborator();

      void doSomething() { dependency.provideSomeService(); }
      long doSomethingElse(int i) { return dependency.getThreadSpecificValue(i); }

      int performComputation(int a, boolean b)
      {
         int i = dependency.getValue();
         List<?> results = dependency.complexOperation(a, i);

         if (b) {
            dependency.setValue(i + results.size());
         }

         return i;
      }
   }

   @SuppressWarnings("UnusedDeclaration")
   static class Collaborator
   {
      static Object xyz;
      protected int value;

      Collaborator() {}
      Collaborator(int value) { this.value = value; }

      @Deprecated private static String doInternal() { return "123"; }

      void provideSomeService() { throw new RuntimeException("Real provideSomeService() called"); }

      int getValue() { return value; }
      void setValue(int value) { this.value = value; }

      List<?> complexOperation(Object input1, Object... otherInputs)
      {
         return input1 == null ? Collections.emptyList() : Arrays.asList(otherInputs);
      }

      final void simpleOperation(int a, String b, Date c) {}

      long getThreadSpecificValue(int i) { return Thread.currentThread().getId() + i; }
   }

   // Mocks without expectations //////////////////////////////////////////////////////////////////////////////////////

   @Test
   public void mockWithNoExpectationsPassingMockClass()
   {
      setUpMocks(MockCollaborator1.class);

      codeUnderTest.doSomething();
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaborator1
   {
      @Mock
      void provideSomeService() {}
   }

   @Test
   public void mockWithNoExpectationsPassingMockInstance()
   {
      setUpMocks(new MockCollaborator1());

      codeUnderTest.doSomething();
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetUpMockClassLackingTheRealClass()
   {
      setUpMocks(Collaborator.class);
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetUpMockForClassLackingAMatchingRealMethod()
   {
      setUpMocks(MockForClassWithoutRealMethod.class);
   }

   @MockClass(realClass = Collaborator.class)
   static final class MockForClassWithoutRealMethod
   {
      @Mock void noMatchingRealMethod() {}
   }

   @Test
   public void setUpMockForSingleClassPassingAnnotatedMockInstance()
   {
      setUpMock(new MockCollaborator1());

      codeUnderTest.doSomething();
   }

   @MockClass(realClass = Collaborator.class)
   public static class MockCollaborator6
   {
      @Mock
      int getValue() { return 1; }
   }

   @Test
   public void setUpMockForSingleRealClassByPassingTheMockClassLiteral()
   {
      setUpMock(MockCollaborator6.class);

      assertEquals(1, new Collaborator().getValue());
   }

   @Test
   public void setUpMockForSingleRealClassByPassingAMockClassInstance()
   {
      setUpMock(new MockCollaborator6());

      assertEquals(1, new Collaborator().getValue());
   }

   interface BusinessInterface { void provideSomeService(); }

   @MockClass(realClass = BusinessInterface.class)
   static class MockCollaborator3
   {
      @Mock
      void provideSomeService() {}
   }

   @Test
   public void setUpMockForInterface()
   {
      BusinessInterface mock = setUpMock(new MockCollaborator3());

      mock.provideSomeService();
   }

   public interface GenericInterface<T>
   {
      void method(T t);
      String method(int[] ii, T l, String[][] ss, T[] ll);
   }
   public interface NonGenericSubInterface extends GenericInterface<Long> {}

   @MockClass(realClass = NonGenericSubInterface.class)
   public static final class MockForNonGenericSubInterface
   {
      @Mock(invocations = 1)
      public void method(Long l) { assertTrue(l > 0); }

      @Mock
      public String method(int[] ii, Long l, String[][] ss, Long[] ll)
      {
         assertTrue(ii.length > 0 && l > 0);
         return "mocked";
      }
   }

   @Test
   public void mockMethodOfSubInterfaceWithGenericTypeArgument()
   {
      NonGenericSubInterface mock = setUpMock(MockForNonGenericSubInterface.class);

      mock.method(123L);
      assertEquals("mocked", mock.method(new int[] {1}, 45L, null, null));
   }

   @Test
   public void mockMethodOfGenericInterfaceWithArrayAndGenericTypeArgument()
   {
      GenericInterface<Long> mock = new MockUp<GenericInterface<Long>>() {
         @Mock
         String method(int[] ii, Long l, String[][] ss, Long[] tt)
         {
            assertTrue(ii.length > 0 && l > 0);
            return "mocked";
         }
      }.getMockInstance();

      assertEquals("mocked", mock.method(new int[] {1}, 45L, null, null));
   }

   @Test
   public void setUpMocksFromInnerMockClassWithMockConstructor()
   {
      setUpMocks(new MockCollaborator4());
      assertFalse(mockExecuted);

      new CodeUnderTest().doSomething();

      assertTrue(mockExecuted);
   }

   @MockClass(realClass = Collaborator.class)
   class MockCollaborator4
   {
      @Mock
      void $init() { mockExecuted = true; }

      @Mock
      void provideSomeService() {}
   }

   @Test
   public void setUpMocksFromMockClassWithStaticMockMethod()
   {
      setUpMocks(MockCollaborator5.class);

      codeUnderTest.doSomething();
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaborator5
   {
      @Mock
      @Deprecated // to check that another annotation doesn't interfere, and to increase coverage
      static void provideSomeService() {}
   }

   // Mocks WITH expectations /////////////////////////////////////////////////////////////////////////////////////////

   @Test
   public void setUpMocksContainingExpectations()
   {
      setUpMocks(MockCollaboratorWithExpectations.class);

      int result = codeUnderTest.performComputation(2, true);

      assertEquals(0, result);
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithExpectations
   {
      @Mock(minInvocations = 1)
      int getValue() { return 0; }

      @Mock(maxInvocations = 2)
      void setValue(int value)
      {
         assertEquals(1, value);
      }

      @Mock
      List<?> complexOperation(Object input1, Object... otherInputs)
      {
         int i = (Integer) otherInputs[0];
         assertEquals(0, i);

         List<Integer> values = new ArrayList<Integer>();
         values.add((Integer) input1);
         return values;
      }

      @Mock(invocations = 0)
      void provideSomeService() {}
   }

   @Test(expected = MissingInvocation.class)
   public void setUpMockWithMinInvocationsExpectationButFailIt()
   {
      setUpMocks(MockCollaboratorWithMinInvocationsExpectation.class);
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithMinInvocationsExpectation
   {
      @Mock(minInvocations = 2)
      int getValue() { return 1; }
   }

   @Test(expected = UnexpectedInvocation.class)
   public void setUpMockWithMaxInvocationsExpectationButFailIt()
   {
      setUpMocks(MockCollaboratorWithMaxInvocationsExpectation.class);

      new Collaborator().setValue(23);
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithMaxInvocationsExpectation
   {
      @Mock(maxInvocations = 0)
      void setValue(int v) { assertEquals(23, v); }
   }

   @Test(expected = UnexpectedInvocation.class)
   public void setUpMockWithInvocationsExpectationButFailIt()
   {
      setUpMocks(MockCollaboratorWithInvocationsExpectation.class);

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithInvocationsExpectation
   {
      @Mock(invocations = 1)
      void provideSomeService() {}
   }

   // Reentrant mocks /////////////////////////////////////////////////////////////////////////////////////////////////

   @Test(expected = RuntimeException.class)
   public void setUpReentrantMock()
   {
      setUpMocks(MockCollaboratorWithReentrantMock.class);

      codeUnderTest.doSomething();
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithReentrantMock
   {
      Collaborator it;

      @Mock(reentrant = false)
      int getValue() { return 123; }

      @Mock(reentrant = true, invocations = 1)
      void provideSomeService() { it.provideSomeService(); }
   }

   // Mocks for constructors and static methods ///////////////////////////////////////////////////////////////////////

   @Test
   public void setUpMockForConstructor()
   {
      setUpMocks(MockCollaboratorWithConstructorMock.class);

      new Collaborator(5);
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithConstructorMock
   {
      @Mock(invocations = 1)
      void $init(int value)
      {
         assertEquals(5, value);
      }
   }

   @Test
   public void setUpMockForStaticMethod()
   {
      setUpMocks(MockCollaboratorForStaticMethod.class);

      //noinspection deprecation
      Collaborator.doInternal();
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorForStaticMethod
   {
      @Mock(invocations = 1)
      static String doInternal() { return ""; }
   }

   @Test
   public void setUpMockForSubclassConstructor()
   {
      setUpMocks(MockSubCollaborator.class);

      new SubCollaborator(31);
   }

   static class SubCollaborator extends Collaborator
   {
      SubCollaborator(int i) { throw new RuntimeException("" + i); }

      @Override
      void provideSomeService() { value = 123; }
   }

   @MockClass(realClass = SubCollaborator.class)
   static class MockSubCollaborator
   {
      @Mock(invocations = 1)
      void $init(int i) { assertEquals(31, i); }

      @SuppressWarnings("UnusedDeclaration")
      native void doNothing();
   }

   @Test
   public void setUpMocksForClassHierarchy()
   {
      new MockUp<SubCollaborator>() {
         SubCollaborator it;

         @Mock void $init(int i) { assertNotNull(it); assertTrue(i > 0); }

         @Mock void provideSomeService() { it.value = 45; }

         @Mock int getValue(Invocation inv)
         {
            assertNotNull(inv.getInvokedInstance());
            // The value of "it" is undefined here; it will be null if this is the first mock invocation reaching this
            // mock class instance, or the last instance of the mocked subclass if a previous invocation of a mock
            // method whose mocked method is defined in the subclass occurred on this mock class instance.
            return 123;
         }
      };

      SubCollaborator collaborator = new SubCollaborator(123);
      collaborator.provideSomeService();
      assertEquals(45, collaborator.value);
      assertEquals(123, collaborator.getValue());
   }

   @Test // Note: this test only works under JDK 1.6+; JDK 1.5 does not support redefining natives.
   public void mockNativeMethodInClassWithRegisterNatives()
   {
      MockSystem mockUp = new MockSystem();
      assertEquals(0, System.nanoTime());

      mockUp.tearDown();
      assertTrue(System.nanoTime() > 0);
   }

   static class MockSystem extends MockUp<System> {
      @Mock public static long nanoTime() { return 0; }
   }

   @Test // Note: this test only works under JDK 1.6+; JDK 1.5 does not support redefining natives.
   public void mockNativeMethodInClassWithoutRegisterNatives() throws Exception
   {
      MockFloat mockUp = new MockFloat();
      assertEquals(0.0, Float.intBitsToFloat(2243019), 0.0);

      mockUp.tearDown();
      assertTrue(Float.intBitsToFloat(2243019) > 0);
   }

   static class MockFloat extends MockUp<Float>
   {
      @SuppressWarnings("UnusedDeclaration")
      @Mock
      public static float intBitsToFloat(int bits) { return 0; }
   }

   static final class AnotherCollaborator
   {
      static boolean doSomething() { throw new IllegalAccessError("Not mocked!"); }
   }

   @MockClass(realClass = AnotherCollaborator.class)
   static final class MockAnotherCollaborator
   {
      @Mock boolean doSomething() { return false; }
   }

   @Test
   public void setUpMockForJREClass()
   {
      MockThread mockThread = new MockThread();
      setUpMocks(mockThread);

      Thread.currentThread().interrupt();

      assertTrue(mockThread.interrupted);
   }

   @MockClass(realClass = Thread.class)
   public static class MockThread
   {
      boolean interrupted;

      @Mock(invocations = 1)
      public void interrupt() { interrupted = true; }
   }

   @Test
   public void mockJREMethodAndConstructorUsingAnnotatedMockClass() throws Exception
   {
      setUpMocks(new MockLoginContext());

      new LoginContext("test", (CallbackHandler) null).login();
   }

   @MockClass(realClass = LoginContext.class)
   public static class MockLoginContext
   {
      @Mock(invocations = 1)
      public void $init(String name, CallbackHandler callbackHandler)
      {
         assertEquals("test", name);
         assertNull(callbackHandler);
      }

      @Mock
      public void login() {}

      @Mock(maxInvocations = 1)
      public Subject getSubject() { return null; }
   }

   @Test(expected = LoginException.class)
   public void mockJREMethodAndConstructorWithMockUpClass() throws Exception
   {
      new MockUp<LoginContext>() {
         @Mock
         void $init(String name) { assertEquals("test", name); }

         @Mock
         void login() throws LoginException
         {
            throw new LoginException();
         }
      };

      new LoginContext("test").login();
   }

   @Test
   public void mockJREClassWithStubs() throws Exception
   {
      setUpMocks(new MockLoginContextWithStubs());

      LoginContext context = new LoginContext("");
      context.login();
      context.logout();
   }

   @MockClass(realClass = LoginContext.class, stubs = {"(String)", "logout"})
   final class MockLoginContextWithStubs
   {
      @Mock(invocations = 1)
      void login() {}
   }

   @Test
   public void mockJREClassWithInverseStubs() throws Exception
   {
      setUpMocks(MockLoginContextWithInverseStubs.class);

      LoginContext context = new LoginContext("", null, null);
      context.login();
      context.logout();
   }

   @MockClass(realClass = LoginContext.class, stubs = "", inverse = true)
   static class MockLoginContextWithInverseStubs
   {
      @Mock(invocations = 1)
      static void login() {}
   }

   // Stubbing of static class initializers ///////////////////////////////////////////////////////////////////////////

   static class ClassWithStaticInitializers
   {
      static String str = "initialized"; // if final it would be a compile-time constant
      static final Object obj = new Object(); // constant, but only at runtime

      static { System.exit(1); }

      static void doSomething() {}

      static
      {
         try {
            Class.forName("NonExistentClass");
         }
         catch (ClassNotFoundException e) {
            e.printStackTrace();
         }
      }
   }

   @Test
   public void mockStaticInitializer()
   {
      new MockUp<ClassWithStaticInitializers>() {
         @Mock(invocations = 1) void $clinit() {}
      };

      ClassWithStaticInitializers.doSomething();

      assertNull(ClassWithStaticInitializers.str);
      assertNull(ClassWithStaticInitializers.obj);
   }

   static class AnotherClassWithStaticInitializers
   {
      static { System.exit(1); }
      static void doSomething() { throw new RuntimeException(); }
   }

   @Test
   public void stubOutStaticInitializer() throws Exception
   {
      setUpMock(new MockForClassWithInitializer());

      AnotherClassWithStaticInitializers.doSomething();
   }

   @MockClass(realClass = AnotherClassWithStaticInitializers.class, stubs = "<clinit>")
   static class MockForClassWithInitializer
   {
      @Mock(minInvocations = 1, maxInvocations = 1)
      void doSomething() {}
   }

   static class YetAnotherClassWithStaticInitializer
   {
      static { System.loadLibrary("none.dll"); }
      static void doSomething() {}
   }

   @SuppressWarnings("ClassMayBeInterface")
   @MockClass(realClass = YetAnotherClassWithStaticInitializer.class, stubs = "<clinit>")
   static class MockForYetAnotherClassWithInitializer {}

   @Test
   public void stubOutStaticInitializerWithEmptyMockClass() throws Exception
   {
      setUpMock(MockForYetAnotherClassWithInitializer.class);

      YetAnotherClassWithStaticInitializer.doSomething();
   }

   @SuppressWarnings("ClassMayBeInterface")
   @MockClass(realClass = MockAnotherCollaborator.class)
   static class MockClassPointingToAnother {}

   // Other tests /////////////////////////////////////////////////////////////////////////////////////////////////////

   @Test
   public void mockJREInterface() throws Exception
   {
      CallbackHandler callbackHandler = setUpMock(new MockCallbackHandler());

      callbackHandler.handle(new Callback[] {new NameCallback("Enter name:")});
   }

   @MockClass(realClass = CallbackHandler.class)
   public static class MockCallbackHandler
   {
      @Mock(invocations = 1)
      public void handle(Callback[] callbacks)
      {
         assertEquals(1, callbacks.length);
         assertTrue(callbacks[0] instanceof NameCallback);
      }
   }

   @Test
   public void mockJREInterfaceWithMockUp() throws Exception
   {
      CallbackHandler callbackHandler = new MockUp<CallbackHandler>() {
         @Mock(invocations = 1)
         void handle(Callback[] callbacks)
         {
            assertEquals(1, callbacks.length);
            assertTrue(callbacks[0] instanceof NameCallback);
         }
      }.getMockInstance();

      callbackHandler.handle(new Callback[] {new NameCallback("Enter name:")});
   }

   @Test
   public void accessMockedInstanceThroughItField() throws Exception
   {
      final Subject testSubject = new Subject();

      new MockUp<LoginContext>()
      {
         LoginContext it;

         @Mock(invocations = 1)
         void $init(String name, Subject subject)
         {
            assertNotNull(name);
            assertSame(testSubject, subject);
            assertNotNull(it);
            setField(it, subject); // forces setting of private field, since no setter is available
         }

         @Mock(invocations = 1)
         void login()
         {
            assertNotNull(it);
            assertNull(it.getSubject()); // returns null until the subject is authenticated
            setField(it, "loginSucceeded", true); // private field set to true when login succeeds
         }

         @Mock(invocations = 1)
         void logout()
         {
            assertNotNull(it);
            assertSame(testSubject, it.getSubject());
         }
      };

      LoginContext theMockedInstance = new LoginContext("test", testSubject);
      theMockedInstance.login();
      theMockedInstance.logout();
   }

   @Test
   public void reenterMockedMethodsThroughItField() throws Exception
   {
      // Create objects to be exercised by the code under test:
      Configuration configuration = new Configuration()
      {
         @Override
         public AppConfigurationEntry[] getAppConfigurationEntry(String name)
         {
            Map<String, ?> options = Collections.emptyMap();
            return new AppConfigurationEntry[]
            {
               new AppConfigurationEntry(
                  TestLoginModule.class.getName(),
                  AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, options)
            };
         }
      };
      LoginContext loginContext = new LoginContext("test", null, null, configuration);

      // Set up mocks:
      ReentrantMockLoginContext mockInstance = new ReentrantMockLoginContext();

      // Exercise the code under test:
      assertNull(loginContext.getSubject());
      loginContext.login();
      assertNotNull(loginContext.getSubject());
      assertTrue(mockInstance.loggedIn);

      mockInstance.ignoreLogout = true;
      loginContext.logout();
      assertTrue(mockInstance.loggedIn);

      mockInstance.ignoreLogout = false;
      loginContext.logout();
      assertFalse(mockInstance.loggedIn);
   }

   static final class ReentrantMockLoginContext extends MockUp<LoginContext>
   {
      LoginContext it;
      boolean ignoreLogout;
      boolean loggedIn;

      @Mock(reentrant = true)
      void login() throws LoginException
      {
         try {
            it.login();
            loggedIn = true;
         }
         finally {
            it.getSubject();
         }
      }

      @Mock(reentrant = true)
      void logout() throws LoginException
      {
         if (!ignoreLogout) {
            it.logout();
            loggedIn = false;
         }
      }
   }

   public static class TestLoginModule implements LoginModule
   {
      public void initialize(
         Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
         Map<String, ?> options)
      {
      }

      public boolean login() { return true; }
      public boolean commit() { return true; }
      public boolean abort() { return false; }
      public boolean logout() { return true; }
   }

   @Test
   public void mockFileConstructor()
   {
      new MockUp<File>() {
         File it;

         @Mock
         void $init(String pathName)
         {
            setField(it, "path", "fixedPrefix/" + pathName);
         }
      };

      File f = new File("test");
      assertEquals("fixedPrefix/test", f.getPath());
   }

   @Test
   public void stubbedOutAnnotatedMethodInMockedClass() throws Exception
   {
      setUpMocks(MockCollaborator7.class);

      assertTrue(Collaborator.class.getDeclaredMethod("doInternal").isAnnotationPresent(Deprecated.class));
   }

   @MockClass(realClass = Collaborator.class, stubs = "doInternal")
   static class MockCollaborator7
   {
      @Mock void provideSomeService() {}
   }

   @Test
   public void concurrentMock() throws Exception
   {
      new MockUp<Collaborator>() {
         @Mock long getThreadSpecificValue(int i) { return Thread.currentThread().getId() + 123; }
      };

      Thread[] threads = new Thread[5];

      for (int i = 0; i < threads.length; i++) {
         threads[i] = new Thread() {
            @Override
            public void run()
            {
               long threadSpecificValue = Thread.currentThread().getId() + 123;
               long actualValue = new CodeUnderTest().doSomethingElse(0);
               assertEquals(threadSpecificValue, actualValue);
            }
         };
      }

      for (Thread thread : threads) { thread.start(); }
      for (Thread thread : threads) { thread.join(); }
   }

   @Test
   public void stubOutMethodsInSpecifiedClassButNotInBaseClass()
   {
      setUpMocks(MockSubclassWithStubbings.class);

      // Stubbing applies to subclass:
      assertEquals(123, new SubCollaborator(5).getValue());

      // But not to base class:
      //noinspection deprecation
      assertEquals("123", Collaborator.doInternal());
   }

   @MockClass(realClass = SubCollaborator.class, stubs = {"(int)", "doInternal"})
   static class MockSubclassWithStubbings
   {
      @Mock
      int getValue() { return 123; }
   }
}
