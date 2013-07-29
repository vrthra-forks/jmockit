/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import java.awt.*;

import org.junit.*;

import mockit.*;

public final class TestsUsingJMockitAPIsTest
{
   public static class A
   {
      public void doSomething() { throw new RuntimeException("should not execute"); }
      int getValue() { return -1; }
      B getB() { return new B(); }
   }

   static final class B
   {
      boolean run(String s) { return s.length() > 0; }
   }

   @Ignore @Test
   public void verifyThatAWTToolkitIsUnaffectedByStubbingInPreviousTestClass()
   {
      assert Toolkit.getDefaultToolkit().getAWTEventListeners() != null;
   }

   @Test
   public void usingTheExpectationsAPI(final A mockedA)
   {
      new Expectations()
      {
         {
            mockedA.getValue(); result = 123;
         }
      };

      assert mockedA.getValue() == 123;
   }

   @Test
   public void usingTheVerificationsAPI(@Injectable final A mockedA)
   {
      mockedA.doSomething();

      new Verifications()
      {
         {
            mockedA.doSomething();
         }
      };
   }

   @Test
   public void cascadedMock(@Cascading final A mock)
   {
      new NonStrictExpectations()
      {
         {
            mock.getB().run(anyString); result = true;
         }
      };

      assert mock.getB().run("");
   }
}
