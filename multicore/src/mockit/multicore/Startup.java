/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.io.*;
import java.lang.instrument.*;

import mockit.multicore.junit.*;

public final class Startup
{
   public static void premain(String agentArgs, Instrumentation inst) throws IOException
   {
      mockit.internal.startup.Startup.initialize(inst, ModifiedRunNotifier.class, ModifiedParentRunner.class);
   }
}
