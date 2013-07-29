/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import mockit.internal.annotations.*;
import mockit.internal.startup.*;

/**
 * Provides static methods for the mocking of arbitrary classes, according to specified <em>mock classes</em> defined in
 * test code.
 * Such methods are intended to be called from test code only.
 * <p/>
 * Once mocked, a "real" method defined in a production class will behave (during test execution) as if its
 * implementation was replaced by a call to the corresponding <em>mock method</em> in the mock class.
 * Whatever value this mock method returns will be the value returned by the call to the mocked method.
 * The mock method can also throw an exception or error, which will then be propagated to the caller of the mocked
 * "real" method.
 * Therefore, while mocked the original code in the real method is never executed (unless explicitly called from inside
 * the mock method).
 * The same basic rules apply to constructors, which can be mocked by mock methods with the special name "$init".
 * <p/>
 * In the Tutorial:
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/StateBasedTesting.html">Writing state-based tests</a>
 *
 * @see MockUp
 */
public final class Mockit
{
   static { Startup.verifyInitialization(); }
   private Mockit() {}

   /**
    * Sets up the mocks defined in one or more {@linkplain MockClass mock classes}.
    * <p/>
    * After this call, all such mocks are "in effect" until the end of the test method inside which it appears, if this
    * is the case.
    * If the method is a "before"/"setUp" method which executes before all test methods, then the mocks will remain in
    * effect until the end of the test (including any "after"/"tearDown" methods).
    * <p/>
    * Any invocation count constraints specified on mock methods (such as {@code @Mock(invocations = 1)}, for example)
    * will be automatically verified after the code under test is executed.
    * <p/>
    * For each call made during test execution to a <em>mocked</em> method, the corresponding <em>mock</em> method is
    * called instead.
    * A mock method must have the same signature (ie, name and parameters) as the corresponding mocked/real method.
    * The return type of the mock method must be the same exact type <em>or</em> a compatible one.
    * The {@code throws} clause may differ in any way.
    * Note also that the mock method can be static or not, independently of the real method being static or not.
    * <p/>
    * A constructor in the real class can be mocked by a corresponding mock method of name {@code $init}, declared
    * with the same parameters and with {@code void} return type.
    * It will be called for each new instance of the real class that is created through a call to that constructor, with
    * whatever arguments are passed to it.
    * <p/>
    * <strong>Class initializers</strong> of the real class (one or more {@code static} initialization blocks plus all
    * assignments to {@code static} fields) can be mocked by providing a mock method named {@code $clinit} in the mock
    * class. This method should return {@code void} and have no declared parameters.
    * It will be called at most once, at the time the real class is initialized by the JVM (and since all static
    * initializers for that class are mocked, the initialization will have no effect).
    * <p/>
    * Mock methods can gain access to the instance of the real class on which the corresponding real method or
    * constructor was called. This requires the mock class to define an instance field of name <strong>"it"</strong>,
    * the same type as the real class, and accessible from that class (in general, this means the field will have to be
    * {@code public}). Such a field will always be set to the appropriate real class instance, whenever a mock method is
    * called. Note that through this field the mock class will be able to call any accessible instance method on the
    * real class, including the real method corresponding to the current mock method. In this case, however, such calls
    * are not allowed by default because they lead to infinite recursion, with the mock calling itself indirectly
    * through the redefined real method. If the real method needs to be called from the mock method, then the latter
    * must be declared as {@linkplain mockit.Mock#reentrant reentrant}.
    *
    * @param mockClassesOrInstances one or more classes ({@code Class} objects) or instances of classes which define
    * arbitrary methods and/or constructors, where the ones annotated as {@linkplain Mock mocks} will be used to
    * redefine corresponding real methods/constructors in a designated {@linkplain MockClass#realClass() real class}
    * (usually, a class on which the code under test depends on)
    *
    * @throws IllegalArgumentException if a given mock class fails to specify the corresponding real class using the
    * {@code @MockClass(realClass = ...)} annotation; or if a mock class defines a mock method for which no
    * corresponding real method or constructor exists in the real class;
    * or if the real method matching a mock method is {@code abstract}
    *
    * @deprecated Use {@link MockUp} instead.
    */
   @Deprecated
   public static void setUpMocks(Object... mockClassesOrInstances)
   {
      for (Object mockClassOrInstance : mockClassesOrInstances) {
         Class<?> mockClass;
         Object mock;

         if (mockClassOrInstance instanceof Class<?>) {
            mockClass = (Class<?>) mockClassOrInstance;
            mock = null;
         }
         else {
            mockClass = mockClassOrInstance.getClass();
            mock = mockClassOrInstance;
         }

         new MockClassSetup(mock, mockClass).redefineMethods();
      }
   }

   /**
    * Sets up the mocks defined in the given mock class.
    * <p/>
    * If the type {@linkplain MockClass#realClass referred to} by the mock class is actually an interface, then a new
    * empty implementation class is created.
    *
    * @param mockClassOrInstance the mock class itself (given by its {@code Class} literal), or an instance of the mock
    * class
    *
    * @return a new instance of the implementation class created for the mocked interface, or {@code null} otherwise
    *
    * @throws IllegalArgumentException if a given mock class fails to specify the corresponding real class using the
    * {@code @MockClass(realClass = ...)} annotation; or if a mock class defines a mock method for which no
    * corresponding real method or constructor exists in the real class;
    * or if the real method matching a mock method is {@code abstract}
    *
    * @see #setUpMocks(Object...)
    *
    * @deprecated Use {@link MockUp#MockUp()} and {@link MockUp#getMockInstance()} (when mocking an interface) instead.
    */
   @Deprecated
   public static <T> T setUpMock(Object mockClassOrInstance)
   {
      Class<?> mockClass;
      Object mock;

      if (mockClassOrInstance instanceof Class<?>) {
         mockClass = (Class<?>) mockClassOrInstance;
         mock = null;
      }
      else {
         mockClass = mockClassOrInstance.getClass();
         mock = mockClassOrInstance;
      }

      Class<T> realClass = MockClassSetup.getRealClass(mockClass);

      if (realClass.isInterface()) {
         return new MockedImplementationClass<T>(mockClass, mock).generate(realClass, null);
      }

      new MockClassSetup(realClass, mock, mockClass).redefineMethods();
      return null;
   }
}
