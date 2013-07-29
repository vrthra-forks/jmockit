/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;

import org.junit.*;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import mockit.internal.*;

@SuppressWarnings("UnusedDeclaration")
public final class ForEachInvocationTest
{
   static class Collaborator
   {
      Collaborator() {}

      Collaborator(int i) {}

      int getValue(int i) { return -i; }
      void doSomething() {}
      void doSomething(int i) {}
      void doSomething(boolean b, int i) {}
      String doSomething(boolean b, int[] i, String s) { return s + b + i[0]; }
      static boolean staticMethod() { return true; }
      static boolean staticMethod(int i) { return i > 0; }
      native long nativeMethod(boolean b);
      final char finalMethod(String s) { return 's'; }
      private void privateMethod(short s) {}
      void addElements(Collection<String> elements) { elements.add("one element"); }
   }

   @Test(expected = AssertionError.class)
   public void recordExpectationWithHandlerThatWillValidateMultipleInvocations(final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.doSomething(anyBoolean, anyInt);
         forEachInvocation = new Object() {
            void validate(boolean b, int i)
            {
               assertTrue(b);
               assertEquals(5, i);
            }
         };
      }};

      mock.doSomething(true, 5); // valid
      mock.doSomething(false, 1); // invalid
   }

   @Test(expected = AssertionError.class)
   public void verifyExpectationWithHandlerThatValidatesMultipleInvocations(final Collaborator mock)
   {
      mock.doSomething(true, 5); // valid
      mock.doSomething(true, 1); // invalid

      new Verifications() {{
         mock.doSomething(anyBoolean, anyInt);
         forEachInvocation = new Object() {
            void validate(boolean b, int i)
            {
               assertTrue(b);
               assertEquals(5, i);
            }
         };
      }};
   }

   @Test
   public void recordExpectationsWithHandlersForEachInvocation()
   {
      Collaborator collaborator = new Collaborator();
      final boolean bExpected = true;
      final int[] iExpected = new int[0];
      final String sExpected = "test";

      new Expectations() {
         Collaborator mock;

         {
            mock.getValue(5); result = 2;
            forEachInvocation = new Object() { boolean isValid(int i) { return i > 2; } };

            mock.doSomething(bExpected, iExpected, sExpected); result = "";
            forEachInvocation = new Object() {
               void invoked(Boolean b, int[] i, String s)
               {
                  assertEquals(bExpected, b);
                  assertArrayEquals(iExpected, i);
                  assertEquals(sExpected, s);
               }
            };
         }
      };

      assertEquals(2, collaborator.getValue(5));
      assertEquals("", collaborator.doSomething(bExpected, iExpected, sExpected));
   }

   @Test
   public void recordExpectationForMultiParameterMethodWithHandlerHavingNoParameters(final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.doSomething(anyBoolean, anyInt); times = 1;
         forEachInvocation = new Object() {
            // Not useful, but allowed for consistency with Delegate methods and with
            // the use of an "Invocation" parameter.
            void validate() {}
         };
      }};

      mock.doSomething(true, 123);
   }

   @Test
   public void recordExpectationForMultiParameterMethodWithHandlerHavingOnlyTheInvocationParameter(
      final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.doSomething(anyBoolean, 123); times = 1;
         forEachInvocation = new Object() {
            void validate(Invocation inv)
            {
               assertSame(mock, inv.getInvokedInstance());
               Object[] args = inv.getInvokedArguments();
               assertEquals(2, args.length);
               assertTrue((Boolean) args[0]);
               assertEquals(123, args[1]);
            }
         };
      }};

      mock.doSomething(true, 123);
   }

   @Test
   public void verifyExpectationsWithHandlersForEachInvocation(final Collaborator mock)
   {
      Collaborator collaborator = new Collaborator();
      collaborator.addElements(asList("a", "B", "c"));
      collaborator.addElements(asList("B", "123"));

      collaborator.doSomething(true, new int[0], "test");

      new Verifications() {{
         //noinspection unchecked
         mock.addElements((Collection<String>) any);
         forEachInvocation = new Object() {
            void verify(Collection<String> elements) { assertTrue(elements.contains("B")); }
         };

         mock.doSomething(anyBoolean, null, null);
         forEachInvocation = new Object() {
            void invoked(Boolean b, int[] i, String s)
            {
               assertTrue(b);
               assertArrayEquals(new int[0], i);
               assertEquals("test", s);
            }
         };
      }};
   }

   @Test
   public void combineDelegateAndValidationObjects(final Collaborator collaborator)
   {
      new NonStrictExpectations() {{
         collaborator.getValue(anyInt);
         result = new Delegate() {
            int getValue(int i) { return i + 1; }
         };
         forEachInvocation = new Object() {
            boolean isNonNegative(int i) { return i >= 0; }
         };
      }};

      assertEquals(1, collaborator.getValue(0));
      assertEquals(2, collaborator.getValue(1));
      assertEquals(3, collaborator.getValue(2));
   }

   @Test
   public void recordExpectationWithHandlerForEachInvocationOfConstructor()
   {
      final ConstructorHandler handler = new ConstructorHandler();

      new Expectations() {
         Collaborator mock;

         {
            new Collaborator(anyInt); forEachInvocation = handler;
         }
      };

      new Collaborator(4);

      assertTrue(handler.capturedArgument > 0);
   }

   static class ConstructorHandler
   {
      int capturedArgument;
      void init(int i) { capturedArgument = i; }
   }

   @Test
   public void verifyExpectationWithHandlerForEachInvocationOfConstructor(Collaborator mock)
   {
      final Collaborator[] collaborators = {new Collaborator(5), new Collaborator(4), new Collaborator(1024)};

      new FullVerifications() {{
         new Collaborator(anyInt);
         forEachInvocation = new Object() {
            void checkIt(Invocation invocation, int i)
            {
               assertTrue(i > 0);
               Collaborator collaborator = collaborators[invocation.getInvocationIndex()];
               assertSame(collaborator, invocation.getInvokedInstance());
               int i2 = (Integer) invocation.getInvokedArguments()[0];
               assertEquals(i, i2);
            }
         };
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyExpectationWithHandlerWhichFailsAssertion(Collaborator mock)
   {
      new Collaborator(0);

      new FullVerifications() {{
         new Collaborator(anyInt);
         forEachInvocation = new Object() {
            void checkIt(int i) { assertTrue(i > 0); }
         };
      }};
   }

   @Test(expected = AssertionError.class)
   public void recordExpectationWithHandlerForStaticMethodWhichAlsoReturnsAValue()
   {
      new Expectations() {
         final Collaborator unused = null;

         {
            Collaborator.staticMethod(anyInt); result = false;
            forEachInvocation = new Object() { boolean staticInvocation(int i) { return i > 0; } };
         }
      };

      assertFalse(Collaborator.staticMethod(-123));
   }

   @Test
   public void verifyExpectationsOnStaticMethodsWithHandlers(Collaborator unused)
   {
      Collaborator.staticMethod();
      Collaborator.staticMethod(1);
      Collaborator.staticMethod(2);
      Collaborator.staticMethod(3);

      new FullVerificationsInOrder() {{
         Collaborator.staticMethod();
         forEachInvocation = new Object() { boolean staticInvocation() { return true; } };

         Collaborator.staticMethod(1);
         forEachInvocation = new Object() { void verify(int i) { assertEquals(1, i); } };

         Collaborator.staticMethod(anyInt); times = 2;
         forEachInvocation = new Object() { void verify(int i) { assertTrue(i == 2 || i == 3); } };
      }};
   }

   @Test
   public void recordExpectationWithInvocationHandlerWhichDefinesStaticHandlerMethod()
   {
      new NonStrictExpectations() {
         Collaborator mock;

         {
            mock.doSomething(anyBoolean, null, null); result = "test";
            //noinspection InstantiationOfUtilityClass
            forEachInvocation = new StaticDelegate();
         }
      };

      assertEquals("test", new Collaborator().doSomething(false, null, "replay"));
   }

   static final class StaticDelegate
   {
      static void verifyArgs(boolean b, int[] i, String s)
      {
         assertFalse(b);
         assertNull(i);
         assertEquals("replay", s);
      }
   }

   @Test
   public void verifyExpectationWithHandlerForNativeMethod(@NonStrict final Collaborator mock)
   {
      new Collaborator().nativeMethod(true);

      new Verifications() {{
         mock.nativeMethod(anyBoolean);
         forEachInvocation = new Object() {
            void verify(boolean b) { assertTrue(b); }
         };
      }};
   }

   @Test
   public void recordExpectationWithHandlerForFinalMethod()
   {
      new Expectations() {
         @NonStrict Collaborator mock;

         {
            mock.finalMethod(anyString); result = 'M';
            forEachInvocation = new Object() { void finalMethod(String s) { assertTrue(s.length() > 0); } };
         }
      };

      assertEquals('M', new Collaborator().finalMethod("testing"));
   }

   @Test
   public void verifyExpectationWithHandlerForPrivateMethod(@NonStrict final Collaborator collaborator)
   {
      collaborator.privateMethod((short) 5);

      new VerificationsInOrder() {{
         invoke(collaborator, "privateMethod", (short) 5); times = 1;
         forEachInvocation = new Object() { void privateMethod(int i) { assertEquals(5, i); } };
      }};
   }

   @Test
   public void recordExpectationWithHandlerForMethodWithCompatibleButDistinctParameterType()
   {
      new Expectations() {
         @NonStrict Collaborator collaborator;

         {
            collaborator.addElements(this.<Collection<String>>withNotNull());
            forEachInvocation = new Object() {
               void addElements(Collection<String> elements) { elements.add("test"); }
            };
         }
      };

      List<String> elements = new ArrayList<String>();
      new Collaborator().addElements(elements);

      assertTrue(elements.contains("test"));
   }

   @Test
   public void recordExpectationWithHandlerDefiningTwoMethods(final Collaborator collaborator)
   {
      new NonStrictExpectations() {{
         collaborator.doSomething(true, null, "str");
         forEachInvocation = new Object() {
            void doSomething(boolean b, int[] i, String s) { assertTrue(b); }
            private String someOther() { return ""; }
         };
      }};

      assertNull(collaborator.doSomething(true, null, "str"));
   }

   @Test
   public void verifyExpectationWithHandlerDefiningTwoNonPrivateMethods(final Collaborator collaborator)
   {
      collaborator.doSomething(true, null, "str");

      new Verifications() {{
         collaborator.doSomething(true, null, "str");

         try {
            forEachInvocation = new Object() {
               void doSomething(boolean b, int[] i, String s) { assertTrue(b); }
               void someOther() {}
            };
            fail();
         }
         catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith(""));
         }
      }};
   }

   @Test
   public void recordExpectationWithHandlerMissingNonPrivateMethod(final Collaborator collaborator)
   {
      new NonStrictExpectations() {{
         collaborator.doSomething(true, null, "str");

         try {
            forEachInvocation = new Object() {
               private String someOther() { return ""; }
               private void doSomethingElse(boolean b, int[] i, String s) {}
            };
            fail();
         }
         catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("No non-private "));
         }
      }};
   }

   @Test(expected = MissingInvocation.class)
   public void handlerForFailedUnorderedVerification(final Collaborator mock)
   {
      mock.doSomething();

      new Verifications() {{
         mock.doSomething(); minTimes = 2;
         forEachInvocation = new Object() { void verify() {} };
      }};
   }

   @Test(expected = UnexpectedInvocation.class)
   public void handlerForFailedOrderedVerification(final Collaborator mock)
   {
      mock.doSomething();

      new VerificationsInOrder() {{
         mock.doSomething(); maxTimes = 0;
         forEachInvocation = new Object() { void verify() {} };
      }};
   }

   @Test
   public void verifyExpectationsInOrderWithHandlerForMultipleInvocations(final Collaborator mock)
   {
      mock.doSomething(1);
      mock.doSomething(2);
      mock.doSomething(3);

      final SequentialInvocationHandler handler = new SequentialInvocationHandler();

      new VerificationsInOrder() {{
         mock.doSomething(anyInt);
         forEachInvocation = handler;
         times = 3;
      }};

      assertEquals(3, handler.index);
   }

   static class SequentialInvocationHandler
   {
      int index;
      void verify(int i) { index++; assertEquals(i, index); }
   }

   @Test
   public void verifyUnorderedExpectationsWithHandlerForMultipleInvocations(final Collaborator mock)
   {
      mock.doSomething(1);
      mock.doSomething(2);
      mock.doSomething(3);

      final SequentialInvocationHandler handler = new SequentialInvocationHandler();

      new FullVerifications() {{
         mock.doSomething(anyInt);
         forEachInvocation = handler;
         times = 3;
      }};

      assertEquals(3, handler.index);
   }

   @Test
   public void verifyExpectationWithHandlerWhichDefinesInvocationParameter(final Collaborator mock)
   {
      mock.doSomething(1);
      mock.doSomething();
      mock.doSomething(2);
      mock.doSomething(3);
      mock.finalMethod("test");
      mock.doSomething(4);

      final Object handler = new Object() {
         void verify(Invocation invocation, int i)
         {
            assertSame(mock, invocation.getInvokedInstance());
            assertEquals(i, invocation.getInvocationCount());
         }
      };

      new VerificationsInOrder() {{
         mock.doSomething(anyInt);
         forEachInvocation = handler;
      }};

      new Verifications() {{
         mock.doSomething(anyInt);
         forEachInvocation = handler;
      }};
   }

   @Test
   public void verifyInvocationsWithHandlersHavingAlsoRecordedExpectations(final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.doSomething(anyInt);

         mock.doSomething(anyBoolean, null, null);
         result = new Delegate() {
            String delegate(boolean b, int[] i, String s)
            {
               assertTrue(b);
               assertNotNull(i);
               assertEquals("test", s);
               return "mocked";
            }
         };
      }};

      assertEquals("mocked", mock.doSomething(true, new int[0], "test"));
      mock.doSomething(1);
      assertEquals("mocked", mock.doSomething(true, new int[0], "test"));

      new Verifications() {{
         mock.doSomething(anyInt); times = 1;
         forEachInvocation = new Object() {
            void validate(int i) { assertEquals(1, i); }
         };
      }};

      new VerificationsInOrder() {{
         mock.doSomething(anyBoolean, null, null);
         forEachInvocation = new Object() {
            void validate(boolean b, int[] i, String s)
            {
               assertTrue(b);
               assertNotNull(i);
               assertEquals("test", s);
            }
         };
      }};
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationsWithBooleanReturningHandler(final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.doSomething(anyInt);
         forEachInvocation = new Object() { boolean isPositive(int i) { return i > 0; } };
      }};

      mock.doSomething(5);
      mock.doSomething(-5);
   }

   @Test(expected = AssertionError.class)
   public void recordAllInvocationsInOrderWithBooleanReturningHandler(final Collaborator mock)
   {
      new Expectations() {{
         mock.doSomething(123);

         mock.doSomething(anyInt); times = 2;
         forEachInvocation = new Object() { boolean isPositive(int i) { return i > 0; } };
      }};

      mock.doSomething(123);
      mock.doSomething(5);
      mock.doSomething(-5);
   }

   @Test(expected = AssertionError.class)
   public void verifyInvocationsWithBooleanReturningHandler(final Collaborator mock)
   {
      mock.doSomething(5);
      mock.doSomething(-5);

      new Verifications() {{
         mock.doSomething(anyInt);
         forEachInvocation = new Object() { boolean isPositive(int i) { return i > 0; } };
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithBooleanReturningHandler(final Collaborator mock)
   {
      mock.doSomething(true, new int[0], "test1");
      mock.doSomething(false, new int[] {1, 2}, "");

      new FullVerifications() {{
         mock.doSomething(anyBoolean, null, null);
         forEachInvocation = new Object() {
            boolean validate(boolean b, int[] i, String s) { return i != null && s.length() > 0; }
         };
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyInvocationsInOrderWithBooleanReturningHandler(final Collaborator mock)
   {
      mock.doSomething(false, null, "test1");
      mock.doSomething(true, new int[0], "test2");
      mock.doSomething(true, null, "");

      new VerificationsInOrder() {{
         mock.doSomething(false, null, anyString);

         mock.doSomething(true, null, anyString);
         forEachInvocation = new Object() {
            boolean validate(boolean b, int[] i, String s) { return b && s.length() > 0; }
         };
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsInOrderWithBooleanReturningHandler(final Collaborator mock)
   {
      mock.doSomething(true, new int[0], "test1");
      mock.doSomething(false, new int[] {1, 2}, "test2");
      mock.doSomething(3);

      new FullVerificationsInOrder() {{
         mock.doSomething(anyBoolean, null, null); times = 2;
         forEachInvocation = new Object() {
            boolean validate(boolean b, int[] i, String s) { return i != null && s.length() > 0; }
         };

         mock.doSomething(anyInt);
         forEachInvocation = new Object() {
            boolean isNegative(int i) { return i < 0; }
         };
      }};
   }

   // Unusual ways to use the forEachInvocation field /////////////////////////////////////////////////////////////////

   @Test
   public void reusableInvocationCounter(final Collaborator mock)
   {
      mock.doSomething(1);
      mock.doSomething(true, 1);
      mock.doSomething(2);
      mock.doSomething(3);
      mock.doSomething(false, 2);

      class InvocationCounter {
         int count;
         void increment() { count++; }
      }

      final InvocationCounter counter = new InvocationCounter();

      new Verifications() {{
         mock.doSomething(anyInt); forEachInvocation = counter;
         mock.doSomething(anyBoolean, anyInt); forEachInvocation = counter;
      }};

      assertEquals(5, counter.count);
   }

   class InvocationCountingVerifications extends Verifications {
      protected int count;
      void increment() { count++; }
   }

   @Test
   public void reusableVerificationsWithInvocationCounter(final Collaborator mock)
   {
      mock.doSomething(1);
      mock.doSomething(true, 1);
      mock.doSomething(2);
      mock.doSomething(3);
      mock.doSomething(false, 2);

      new InvocationCountingVerifications() {{
         mock.doSomething(anyInt); forEachInvocation = this;
         assertEquals(3, count);

         mock.doSomething(anyBoolean, anyInt); forEachInvocation = this;
         assertEquals(5, count);
      }};
   }
}