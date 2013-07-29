/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.incremental.junit4;

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;
import java.io.*;

import static java.lang.reflect.Modifier.*;

import org.junit.internal.runners.*;
import org.junit.runner.Description;
import org.junit.runner.manipulation.*;
import org.junit.runner.notification.*;
import org.junit.runners.*;
import org.junit.runners.model.*;
import org.junit.*;

import mockit.*;
import mockit.incremental.*;

/**
 * JUnit 4 test runner which ignores tests not affected by current local changes in production code.
 * <p/>
 * This allows incremental execution of tests according to the changes made to production code,
 * instead of running the full suite of tests covering such code every time.
 */
public final class IncrementalJUnit4Runner extends MockUp<ParentRunner<?>>
{
   private final Properties coverageMap;
   private final Map<Method, Boolean> testMethods;
   private RunNotifier runNotifier;
   private Method testMethod;

   public IncrementalJUnit4Runner()
   {
      File testRunFile = new File("testRun.properties");

      if (testRunFile.exists() && !testRunFile.canWrite()) {
         throw new IllegalStateException();
      }

      coverageMap = new Properties();
      testMethods = new HashMap<Method, Boolean>();
   }

   @Mock
   public void run(Invocation invocation, RunNotifier notifier)
   {
      runNotifier = notifier;

      CoverageInfoFile coverageFile = new CoverageInfoFile(coverageMap);
      invocation.proceed();
      coverageFile.saveToFile();
   }

   @Mock
   public boolean shouldRun(Invocation invocation, Filter filter, Object m)
   {
      testMethod = null;

      if (!coverageMap.isEmpty()) {
         if (m instanceof JUnit38ClassRunner) {
            boolean noTestsToRun = verifyTestMethodsInJUnit38TestClassThatShouldRun((JUnit38ClassRunner) m);

            if (noTestsToRun) {
               return false;
            }
         }
         else if (m instanceof FrameworkMethod) {
            testMethod = ((FrameworkMethod) m).getMethod();
            Boolean shouldRun = shouldRunTestInCurrentTestRun(Test.class, testMethod);

            if (shouldRun != null) {
               return shouldRun;
            }
         }
      }

      Boolean shouldRun = invocation.proceed();

      if (testMethod != null) {
         testMethods.put(testMethod, shouldRun);
      }

      return shouldRun;
   }

   private boolean verifyTestMethodsInJUnit38TestClassThatShouldRun(JUnit38ClassRunner runner)
   {
      Description testClassDescription = runner.getDescription();
      Class<?> testClass = testClassDescription.getTestClass();
      Iterator<Description> itr = testClassDescription.getChildren().iterator();

      while (itr.hasNext()) {
         Description testDescription = itr.next();
         String testMethodName = testDescription.getMethodName();
         testMethod = findPublicVoidMethod(testClass, testMethodName);

         Boolean shouldRun = shouldRunTestInCurrentTestRun(null, testMethod);

         if (shouldRun != null && !shouldRun) {
            itr.remove();
         }
      }

      return testClassDescription.getChildren().isEmpty();
   }

   private Method findPublicVoidMethod(Class<?> aClass, String methodName)
   {
      for (Method method : aClass.getDeclaredMethods()) {
         if (
            isPublic(method.getModifiers()) && method.getReturnType() == void.class &&
            methodName.equals(method.getName())
         ) {
            return method;
         }
      }

      return null;
   }

   private Boolean shouldRunTestInCurrentTestRun(Class<? extends Annotation> testAnnotation, Method testMethod)
   {
      Boolean shouldRun = testMethods.get(testMethod);

      if (shouldRun != null) {
         return shouldRun;
      }

      if (isTestNotApplicableInCurrentTestRun(testAnnotation, testMethod)) {
         reportTestAsNotApplicableInCurrentTestRun(testMethod);
         testMethods.put(testMethod, false);
         return false;
      }

      return null;
   }

   private boolean isTestNotApplicableInCurrentTestRun(Class<? extends Annotation> testAnnotation, Method testMethod)
   {
      return 
         (testAnnotation == null || testMethod.getAnnotation(testAnnotation) != null) &&
         new TestFilter(coverageMap).shouldIgnoreTestInCurrentTestRun(testMethod);
   }

   private static final class TestNotApplicable extends RuntimeException
   {
      private TestNotApplicable() { super("unaffected by changes since last test run"); }
      @Override public void printStackTrace(PrintWriter s) {}
   }

   private static final Throwable NOT_APPLICABLE = new TestNotApplicable();

   private void reportTestAsNotApplicableInCurrentTestRun(Method method)
   {
      Class<?> testClass = method.getDeclaringClass();
      Description testDescription = Description.createTestDescription(testClass, method.getName());

      runNotifier.fireTestAssumptionFailed(new Failure(testDescription, NOT_APPLICABLE));
   }
}
