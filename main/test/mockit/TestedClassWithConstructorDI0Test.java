/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

@SuppressWarnings({"UnusedDeclaration", "UnusedParameters", "ClassWithTooManyFields"})
public final class TestedClassWithConstructorDI0Test
{
   public static final class TestedClassWithConstructorHavingPrimitiveParameter
   {
      public TestedClassWithConstructorHavingPrimitiveParameter(int i) { assertEquals(123, i); }
   }

   public static final class TestedClassWithConstructorHavingStringParameter
   {
      public TestedClassWithConstructorHavingStringParameter(String s) {}
   }

   public static final class TestedClassWithConstructorHavingArrayParameter
   {
      public TestedClassWithConstructorHavingArrayParameter(String[] arr)
      {
         assertArrayEquals(new String[] {"abc", "Xyz"}, arr);
      }
   }

   public static final class TestedClassWithConstructorHavingMultipleLongParameters
   {
      public TestedClassWithConstructorHavingMultipleLongParameters(long l1, long l2)
      {
         assertEquals(1, l1);
         assertEquals(2, l2);
      }

      TestedClassWithConstructorHavingMultipleLongParameters(int i, long l1, long l2)
      {
         throw new RuntimeException("Must not occur");
      }
   }

   public static final class TestedClassWithConstructorHavingVarargsParameter
   {
      public TestedClassWithConstructorHavingVarargsParameter(byte b, char c, String s, byte b2, boolean... flags)
      {
         assertEquals(56, b);
         assertEquals(57, b2);
         assertEquals('X', c);
         assertEquals("test", s);
         assertEquals(3, flags.length);
         assertTrue(flags[0]);
         assertFalse(flags[1]);
         assertTrue(flags[2]);
      }
   }

   @Tested TestedClassWithConstructorHavingPrimitiveParameter tested0;
   @Tested TestedClassWithConstructorHavingStringParameter tested1;
   @Tested TestedClassWithConstructorHavingArrayParameter tested2;
   @Tested TestedClassWithConstructorHavingMultipleLongParameters tested3;
   @Tested TestedClassWithConstructorHavingVarargsParameter tested4;

   @Injectable int i = 123;
   @Injectable int unused;
   @Injectable long l1 = 1;
   @Injectable final long l2 = 2;
   @Injectable String[] arr = {"abc", "Xyz"};
   @Injectable byte b = 56;
   @Injectable byte b2 = 57;
   @Injectable char c = 'X';
   @Injectable String s = "test"; // String is mocked

   // For varargs parameter:
   @Injectable boolean firstFlag = true;
   @Injectable("false") boolean secondFlag;
   @Injectable boolean thirdFlag = true;

   @Test
   public void verifyInstantiationOfTestedObjectsThroughConstructorsWithNonMockedParameters()
   {
      assertNotNull(tested0);
      assertNotNull(tested1);
      assertNotNull(tested2);
      assertNotNull(tested3);
      assertNotNull(tested4);

      // With "String" fully mocked by default, infinite recursion occurred (until a stack overflow).
      new Expectations() {
         Collaborator mock;

         {
            Collaborator.doSomething();
         }
      };

      Collaborator.doSomething();
   }

   static class Collaborator { static void doSomething() {} }
}
