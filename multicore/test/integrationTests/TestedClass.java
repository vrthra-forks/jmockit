/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

public class TestedClass
{
   public static void doSomething(boolean incrementCounter)
   {
      new Dependency().doSomethingElse(incrementCounter);
      assert Dependency.counter == 1 : "counter = " + Dependency.counter;
   }
}
