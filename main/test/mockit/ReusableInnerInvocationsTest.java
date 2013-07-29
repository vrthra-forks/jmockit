/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;

import org.junit.*;
import static org.junit.Assert.*;

@SuppressWarnings("UnusedDeclaration")
public final class ReusableInnerInvocationsTest
{
   @Injectable PrintWriter mock;

   class NestedVerifications extends Verifications
   {
      protected int count;
      void increment() { count++; }
   }

   @Test
   public void reusingNestedVerifications()
   {
      mock.append('c');

      new NestedVerifications() {{
         mock.append(anyChar); forEachInvocation = this;
         assertEquals(1, count);
      }};
   }

   class InnerVerifications extends Verifications
   {
      protected int count;
      void increment() { count++; }
   }

   @Test
   public void reusingInnerVerifications()
   {
      mock.print(2);
      mock.println(true);

      new InnerVerifications() {{
         mock.print((int) anyInt); forEachInvocation = this;
         mock.println((boolean) anyBoolean); forEachInvocation = this;
         assertEquals(2, count);
      }};
   }

   @Test
   public void reusingLocalVerifications()
   {
      class LocalVerifications extends Verifications
      {
         protected int count;
         void increment() { count++; }
      }

      mock.println(true);
      mock.print(1);

      new LocalVerifications() {{
         mock.print((int) anyInt);
         forEachInvocation = this;
         mock.println((boolean) anyBoolean); forEachInvocation = this;
         assertEquals(2, count);
      }};
   }
}

