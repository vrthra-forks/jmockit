/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class FourthTest
{
   @BeforeClass
   public static void initializeCounter()
   {
      Dependency.counter = 0;
   }

   @Test
   public void slowTest1() throws Exception
   {
      TestedClass.doSomething(true);
      Thread.sleep(1000);
   }
}
