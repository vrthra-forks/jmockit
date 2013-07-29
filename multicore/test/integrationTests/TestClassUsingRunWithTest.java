/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

@RunWith(Parameterized.class)
public final class TestClassUsingRunWithTest
{
   @Parameterized.Parameters
   public static List<Integer[]> parameters()
   {
      Integer[][] data = {{1, 1}, {2, 2}, {3, 3}};
      return Arrays.asList(data);
   }

   final int input;
   final int expected;

   public TestClassUsingRunWithTest(int input, int expected)
   {
      this.input = input;
      this.expected = expected;
   }

   @Test
   public void test1()
   {
      A.doSomething();
      assertEquals(expected, input);
   }

   @Test
   public void test2()
   {
      new B().toString();
      assertEquals(expected, input);
   }

   public static class A
   {
      public static void doSomething() { new B(); }
   }

   public static class B
   {
      @Override
      public String toString() { return "B"; }
   }
}
