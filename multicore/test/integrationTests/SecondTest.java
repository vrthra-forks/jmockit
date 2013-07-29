/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class SecondTest
{
   @Before
   public void setUp()
   {
//      assert false : "Failure from setUp method";
   }

   @Test
   public void anotherSlowTest1() throws Exception
   {
      Thread.sleep(400);
      A.doSomething();
   }

   @Test
   public void anotherSlowTest2() throws Exception
   {
      Thread.sleep(600);
      new B().doSomethingElse();
      assert B.counter == 2;
   }

   public static class A
   {
      public static void doSomething()
      {
         new B().doSomethingElse();
         assert B.counter == 1 : "counter = " + B.counter;
      }
   }

   public static class B
   {
      static int counter;
      public void doSomethingElse() { counter++; }
   }
}
