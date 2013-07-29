/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

import mockit.*;

public final class ThirdTest
{
   final A a = new A();

   @Test
   public void firstSlowTest() throws Exception
   {
      a.doSomething();
      Thread.sleep(450);
   }

   @Test
   public void secondSlowTest() throws Exception
   {
      Thread.sleep(250);
      a.doSomething();
   }

   @Test
   public void thirdSlowTest() throws Exception
   {
      Thread.sleep(150);
      a.doSomething();
      Thread.sleep(150);
   }

   public static class A
   {
      public void doSomething() {}
   }
}
