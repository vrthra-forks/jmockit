/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import junit.framework.*;

public final class JUnit38StyleTest extends TestCase
{
   static boolean setUp;

   public JUnit38StyleTest() { super("JUnit 38 test"); }

   @Override
   protected void setUp() throws InterruptedException
   {
      assert !setUp;
      setUp = true;
      Thread.sleep(500);
   }

   @Override
   protected void tearDown()
   {
      assert setUp;
      setUp = false;
   }

   public void test1()
   {
      A.doSomething();
   }

   public void test2()
   {
      new B().doSomethingElse();
      assert B.counter == 2;
   }

   public static class A
   {
      public static void doSomething()
      {
         new B().doSomethingElse();
         assert B.counter == 1;
      }
   }

   public static class B
   {
      static int counter;
      public void doSomethingElse() { counter++; }
   }
}
