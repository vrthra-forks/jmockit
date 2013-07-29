/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore.junit;

import org.junit.runner.*;
import org.junit.runner.notification.*;

import mockit.*;
import mockit.multicore.*;

public final class ModifiedRunNotifier extends MockUp<RunNotifier>
{
   public ModifiedRunNotifier()
   {
      if (MultiCoreTestRunner.inactive) {
         throw new UnsupportedOperationException();
      }
   }

   @Mock
   public static void $init(Invocation inv)
   {
      JUnitTestClassRunnerTask.runNotifier = inv.getInvokedInstance();
      inv.proceed();
   }

   @Mock
   public static void fireTestRunFinished(Invocation inv, Result result)
   {
      // TODO: this condition is needed here because of the RunNotifierDecorator startup mock, another reentrant
      // mock for the same RunNotifier method; ideally, this should be handled automatically.
      if (inv.getInvocationIndex() == 0) {
         RunNotifier runNotifier = inv.getInvokedInstance();
         runAllTests(runNotifier, result);
      }

      inv.proceed();
   }

   private static void runAllTests(RunNotifier runNotifier, Result result)
   {
      // Maven requires this:
      runNotifier.addListener(result.createListener());

      MultiCoreTestRunner.instance().runAllTasksToCompletion();
   }
}
