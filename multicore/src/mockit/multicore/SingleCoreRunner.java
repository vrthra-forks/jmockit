/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import org.junit.runner.*;
import org.junit.runner.notification.*;

public final class SingleCoreRunner extends RunListener
{
   public static void main(String[] args) throws Exception
   {
      MultiCoreTestRunner.inactive = true;
      new SingleCoreRunner().runTestClassesAsTheyAreReceived();
   }

   private void runTestClassesAsTheyAreReceived() throws ClassNotFoundException
   {
      JUnitCore junit = new JUnitCore();
      junit.addListener(new SubProcessEventListener());

      StreamIO streamIO = new StreamIO(System.in, null);
      String testClassName;

      while ((testClassName = streamIO.readNextLine()) != null) {
         Class<?> testClass = Class.forName(testClassName);
         junit.run(testClass);
      }
   }
}
