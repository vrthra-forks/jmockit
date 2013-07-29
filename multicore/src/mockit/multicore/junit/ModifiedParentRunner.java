/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore.junit;

import org.junit.runners.*;
import org.junit.runners.model.*;

import mockit.*;
import mockit.multicore.*;

public final class ModifiedParentRunner extends MockUp<ParentRunner<?>>
{
   public ModifiedParentRunner()
   {
      if (MultiCoreTestRunner.inactive) {
         throw new UnsupportedOperationException();
      }
   }

   @Mock
   public static void validate(Invocation inv)
   {
      ParentRunner<?> runner = inv.getInvokedInstance();
      final Class<?> testClass = runner.getTestClass().getJavaClass();

      if (testClass != null) {
         runner.setScheduler(new RunnerScheduler() {
            private JUnitTestClassRunnerTask task;

            public void schedule(Runnable childStatement)
            {
               if (task == null) {
                  task = new JUnitTestClassRunnerTask(testClass);
                  MultiCoreTestRunner.instance().addTask(task);
               }
            }

            public void finished() {}
         });
      }

      inv.proceed();
   }
}
