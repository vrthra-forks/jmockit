/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import static mockit.Mockit.*;
import static org.junit.Assert.*;
import org.junit.*;

import mockit.*;

public final class CTest
{
   public static class E
   {
      static boolean noReturnCalled;

      @Mock
      public int i()
      {
         System.out.println("E.i");
         return 2;
      }

      @Mock
      public void noReturn()
      {
         noReturnCalled = true;
      }
   }

   @Test
   public void primitiveLongAndIntParameters()
   {
      setUpMocks(PrimitiveLongAndIntParametersMock.class);
      PrimitiveLongAndIntParametersMock.sum = 0;
      C.validateValues(1L, 2);
      assertEquals(3L, PrimitiveLongAndIntParametersMock.sum);
   }

   @MockClass(realClass = C.class)
   public static class PrimitiveLongAndIntParametersMock
   {
      static long sum;

      @Mock
      public static void validateValues(long v1, int v2)
      {
         sum = v1 + v2;
      }
   }

   @Test
   public void constructor1Arg()
   {
      setUpMocks(F2.class);

      C c = new C("test");
      assertNull(c.getSomeValue());
   }

   @MockClass(realClass = C.class)
   public static class F2
   {
      @Mock
      public void $init(String someValue)
      {
         // do nothing
      }
   }

   @Test
   public void privateStaticVoidNoArgs()
   {
      setUpMocks(new H());

      C.printText();
      assertEquals("H.doPrintText", C.printedText);
   }

   @MockClass(realClass = C.class)
   public static class H
   {
      @Mock
      public static void doPrintText() { C.printedText = "H.doPrintText"; }
   }

   public static final class D
   {
      @Mock
      public static boolean b()
      {
         return false;
      }
   }

   public static class N
   {
      @Mock
      public String getCode() { return null; }
   }
}
