/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.standalone;

import java.io.*;
import java.lang.instrument.*;

import mockit.coverage.*;

public final class Startup
{
   private static Instrumentation instrumentation;
   private static boolean inATestRun = true;

   public static void premain(String agentArgs, Instrumentation inst)
   {
      instrumentation = inst;
      inATestRun = determineIfInATestRunOrNot();

      String startupMessage;

      if (inATestRun) {
         startupMessage = "JMockit Coverage tool loaded";
      }
      else {
         startupMessage = "JMockit Coverage tool loaded; connect with a JMX client to control operation";
         CoverageControl.create();
      }

      inst.addTransformer(CodeCoverage.create(inATestRun));

      System.out.println();
      System.out.println(startupMessage);
      System.out.println();
   }

   private static boolean determineIfInATestRunOrNot()
   {
      String[] classPathEntries = System.getProperty("java.class.path").split(File.pathSeparator);

      for (String cpEntry : classPathEntries) {
         if (cpEntry.endsWith(".jar") && (cpEntry.contains("junit-") || cpEntry.contains("testng-"))) {
            return true;
         }
      }

      return false;
   }

   public static Instrumentation instrumentation()
   {
      if (instrumentation == null) {
         instrumentation = mockit.internal.startup.Startup.instrumentation();
      }

      return instrumentation;
   }

   public static boolean isTestRun() { return inATestRun; }
}
