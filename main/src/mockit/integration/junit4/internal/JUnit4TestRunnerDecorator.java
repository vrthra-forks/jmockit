/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4.internal;

import java.lang.reflect.*;

import org.junit.*;
import org.junit.runners.Suite.*;
import org.junit.runners.model.*;

import mockit.integration.internal.*;
import mockit.internal.expectations.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

final class JUnit4TestRunnerDecorator extends TestRunnerDecorator
{
   /**
    * A "volatile boolean" is as good as a java.util.concurrent.atomic.AtomicBoolean here,
    * since we only need the basic get/set operations.
    */
   private volatile boolean shouldPrepareForNextTest = true;

   Object invokeExplosively(FrameworkMethod it, Object target, Object... params) throws Throwable
   {
      Method method = it.getMethod();
      Class<?> testClass = target == null ? method.getDeclaringClass() : target.getClass();

      handleMockingOutsideTestMethods(it, target, testClass);

      // In case it isn't a test method, but a before/after method:
      if (it.getAnnotation(Test.class) == null) {
         if (shouldPrepareForNextTest && it.getAnnotation(Before.class) != null) {
            prepareForNextTest();
            shouldPrepareForNextTest = false;
         }

         TestRun.setRunningIndividualTest(target);
         TestRun.setSavePointForTestMethod(null);

         try {
            return it.invokeExplosively(target, params);
         }
         catch (Throwable t) {
            RecordAndReplayExecution.endCurrentReplayIfAny();
            StackTrace.filterStackTrace(t);
            throw t;
         }
         finally {
            if (it.getAnnotation(After.class) != null) {
               shouldPrepareForNextTest = true;
            }
         }
      }

      if (shouldPrepareForNextTest) {
         prepareForNextTest();
      }

      shouldPrepareForNextTest = true;

      try {
         executeTestMethod(it, target, params);
         return null; // it's a test method, therefore has void return type
      }
      catch (Throwable t) {
         StackTrace.filterStackTrace(t);
         throw t;
      }
      finally {
         TestRun.finishCurrentTestExecution(true);
      }
   }

   private void handleMockingOutsideTestMethods(FrameworkMethod it, Object target, Class<?> testClass)
   {
      TestRun.enterNoMockingZone();

      try {
         if (target == null) {
            Class<?> currentTestClass = TestRun.getCurrentTestClass();

            if (currentTestClass != null && testClass.isAssignableFrom(currentTestClass)) {
               if (it.getAnnotation(AfterClass.class) != null) {
                  cleanUpMocksFromPreviousTestClass();
               }
            }
            else if (testClass.isAnnotationPresent(SuiteClasses.class)) {
               setUpClassLevelMocksAndStubs(testClass);
            }
            else if (it.getAnnotation(BeforeClass.class) != null) {
               updateTestClassState(null, testClass);
            }
         }
         else {
            updateTestClassState(target, testClass);
         }
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }

   private void executeTestMethod(FrameworkMethod it, Object target, Object... parameters) throws Throwable
   {
      SavePoint savePoint = new SavePoint();
      TestRun.setSavePointForTestMethod(savePoint);

      Method testMethod = it.getMethod();
      Throwable testFailure = null;
      boolean testFailureExpected = false;

      try {
         Object[] mockParameters = createInstancesForMockParameters(target, testMethod, parameters, savePoint);
         createInstancesForTestedFields(target);

         TestRun.setRunningIndividualTest(target);
         it.invokeExplosively(target, mockParameters == null ? parameters : mockParameters);
      }
      catch (Throwable thrownByTest) {
         testFailure = thrownByTest;
         Class<? extends Throwable> expectedType = testMethod.getAnnotation(Test.class).expected();
         testFailureExpected = expectedType.isAssignableFrom(thrownByTest.getClass());
      }
      finally {
         concludeTestMethodExecution(savePoint, testFailure, testFailureExpected);
      }
   }
}
