/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.io.*;

public abstract class TestClassRunnerTask
{
   protected final Class<?> testClass;
   protected StreamIO streamIO;

   protected TestClassRunnerTask(Class<?> testClass) { this.testClass = testClass; }

   final void runTestClass(StreamIO streamIO, MultiCoreTestRunner testRunner)
   {
      this.streamIO = streamIO;
      streamIO.writeLine(testClass.getName());
      processTestEventsReceivedFromSubProcess(testRunner);
   }

   private void processTestEventsReceivedFromSubProcess(MultiCoreTestRunner testRunner)
   {
      InputStream in = streamIO.input;

      while (true) {
         TestEvent testEvent = TestEvent.readNext(in);

         if (testEvent == null) {
            String line = streamIO.readNextLineIncludingTerminator();
            if (line == null) break;
            testRunner.registerPendingOutputFromSubProcess(line);
         }
         else if (testEvent == TestEvent.NoMoreTests) {
            break;
         }
         else {
            handleTestEvent(testEvent);
         }
      }
   }

   protected abstract void handleTestEvent(TestEvent testEvent);

   @Override
   public final String toString() { return testClass.getName(); }
}
