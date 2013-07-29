/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

public final class Dependency
{
   static int counter;

   public void doSomethingElse(boolean increment)
   {
      if (increment) {
         counter++;
      }
   }
}
