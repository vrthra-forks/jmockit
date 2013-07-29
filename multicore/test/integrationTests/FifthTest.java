/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class FifthTest
{
   @Before
   public void setUp()
   {
      System.out.println("5 > setUp");
   }

   @Test
   public void slowTest1() throws Exception
   {
      TestedClass.doSomething(true);
      Thread.sleep(667);
      System.out.println("slow test 1 finished");
   }

   @Test
   public void slowTest2() throws Exception
   {
      Thread.sleep(333);
      TestedClass.doSomething(false);
   }
}
