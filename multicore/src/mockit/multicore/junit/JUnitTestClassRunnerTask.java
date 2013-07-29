/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore.junit;

import org.junit.runner.*;
import org.junit.runner.notification.*;

import mockit.multicore.*;

import static mockit.multicore.TestEvent.*;

final class JUnitTestClassRunnerTask extends TestClassRunnerTask
{
   static RunNotifier runNotifier;
   private Description testDescription;

   JUnitTestClassRunnerTask(Class<?> testClass) { super(testClass); }

   @Override
   protected void handleTestEvent(TestEvent testEvent)
   {
      String methodNameOrNames = streamIO.readNextLine();
      notifyJUnitAboutTestEvent(testEvent, methodNameOrNames);
   }

   private void notifyJUnitAboutTestEvent(TestEvent testEvent, String methodNameOrNames)
   {
      if (testDescription == null) {
         testDescription = recreateTestDescription(methodNameOrNames);
      }

      switch (testEvent) {
         case TestStarted: runNotifier.fireTestStarted(testDescription); return;
         case TestFinished: runNotifier.fireTestFinished(testDescription); break;
         case TestIgnored: runNotifier.fireTestIgnored(testDescription); break;
         default:
            Throwable thrownException = streamIO.readObject();
            Failure failure = new Failure(testDescription, thrownException);
            if (testEvent == TestFailure) runNotifier.fireTestFailure(failure);
            else runNotifier.fireTestAssumptionFailed(failure);
      }

      testDescription = null;
   }

   private Description recreateTestDescription(String methodName)
   {
      if (methodName.indexOf(',') < 0) {
         return Description.createTestDescription(testClass, methodName);
      }

      Description compositeDescription = Description.createSuiteDescription(testClass);

      for (String childName : methodName.split(",")) {
         if (childName.length() > 0) {
            Description childDescription = Description.createTestDescription(testClass, childName);
            compositeDescription.addChild(childDescription);
         }
      }

      return compositeDescription;
   }
}
