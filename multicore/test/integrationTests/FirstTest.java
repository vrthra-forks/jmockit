/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class FirstTest
{
   @Test
   public void slowTest1() throws Exception
   {
      A.doSomething();
      Thread.sleep(650);
   }

   @Test
   public void slowTest2() throws Exception
   {
      new B().doSomethingElse();
      assert B.counter == 2;
      Thread.sleep(350);
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

      public void doSomethingElse()
      {
         counter++;
      }
   }
}
