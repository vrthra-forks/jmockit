/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;

import junit.framework.*;
import static org.junit.Assert.*;

@SuppressWarnings("UnusedDeclaration")
public final class DelegateTest extends TestCase
{
   static class Collaborator
   {
      Collaborator() {}
      Collaborator(int i) {}

      int getValue() { return -1; }
      String doSomething(boolean b, int[] i, String s) { return s + b + i[0]; }
      static boolean staticMethod() { return true; }
      static boolean staticMethod(int i) { return i > 0; }
      native long nativeMethod(boolean b);
      final char finalMethod() { return 's'; }
      private float privateMethod() { return 1.2F; }
      void addElements(Collection<String> elements) { elements.add("one element"); }
      Foo getFoo() { return null; }
   }

   static final class Foo { int doSomething() { return 1; } }

   public void testReturnsDelegate()
   {
      Collaborator collaborator = new Collaborator();
      final boolean bExpected = true;
      final int[] iExpected = new int[0];
      final String sExpected = "test";

      new Expectations() {
         Collaborator mock;

         {
            mock.getValue(); result = new Delegate() { int getValue() { return 2; } };

            mock.doSomething(bExpected, iExpected, sExpected);
            result = new Delegate() {
               String doSomething(boolean b, int[] i, String s)
               {
                  assertEquals(bExpected, b);
                  assertArrayEquals(iExpected, i);
                  assertEquals(sExpected, s);
                  return "";
               }
            };
         }
      };

      assertEquals(2, collaborator.getValue());
      assertEquals("", collaborator.doSomething(bExpected, iExpected, sExpected));
   }

   private void testDoSome()
   {
      throw new RuntimeException("Must not be executed");
   }

   public void testConsecutiveReturnValuesThroughDelegatesUsingSeparateReturns()
   {
      new NonStrictExpectations() {
         Collaborator mock;

         {
            mock.getValue();
            result = new Delegate() { int getValue() { return 1; } };
            result = new Delegate() { int getValue() { return 2; } };
         }
      };

      Collaborator collaborator = new Collaborator();
      assertEquals(1, collaborator.getValue());
      assertEquals(2, collaborator.getValue());
   }

   public void testConsecutiveReturnValuesThroughDelegatesUsingSingleReturnsWithVarargs()
   {
      Collaborator collaborator = new Collaborator();
      final int[] array = {1, 2};

      new NonStrictExpectations() {
         Collaborator mock;

         {
            mock.doSomething(true, array, "");
            returns(
               new Delegate() {
                  String execute(boolean b, int[] i, String s)
                  {
                     assertEquals(1, i[0]);
                     return "a";
                  }
               },
               new Delegate() {
                  String execute(boolean b, int[] i, String s)
                  {
                     assertEquals(2, i[0]);
                     return "b";
                  }
               });
         }
      };

      assertEquals("a", collaborator.doSomething(true, array, ""));

      array[0] = 2;
      assertEquals("b", collaborator.doSomething(true, array, ""));
   }

   public void testReturnsMultipleReturnValuesThroughSingleDelegate(final Collaborator collaborator)
   {
      new NonStrictExpectations() {{
         collaborator.getValue();
         result = new Delegate() {
            int i = 1;
            int getValue() { return i++; }
         };
      }};

      assertEquals(1, collaborator.getValue());
      assertEquals(2, collaborator.getValue());
      assertEquals(3, collaborator.getValue());
   }

   public void testDelegateForConstructor()
   {
      final ConstructorDelegate delegate = new ConstructorDelegate();

      new Expectations() {
         Collaborator mock;

         {
            new Collaborator(anyInt); result = delegate;
         }
      };

      new Collaborator(4);

      assertTrue(delegate.capturedArgument > 0);
   }

   static class ConstructorDelegate implements Delegate<Void>
   {
      int capturedArgument;

      void $init(int i) { capturedArgument = i; }
   }

   public void testDelegateForStaticMethod()
   {
      new Expectations() {
         final Collaborator unused = null;

         {
            Collaborator.staticMethod();
            result = new Delegate() { boolean staticMethod() { return false; } };
         }
      };

      assertFalse(Collaborator.staticMethod());
   }

   public void testDelegateWithStaticMethods()
   {
      new NonStrictExpectations() {
         Collaborator mock;

         {
            Collaborator.staticMethod(anyInt); result = new StaticDelegate();

            mock.doSomething(anyBoolean, null, null); result = new StaticDelegate();
         }
      };

      assertTrue(Collaborator.staticMethod(34));
      assertEquals("test", new Collaborator().doSomething(false, null, "replay"));
   }

   static final class StaticDelegate implements Delegate<Object>
   {
      static boolean staticMethod(int i)
      {
         assertEquals(34, i);
         return true;
      }
      
      static String doSomething(boolean b, int[] i, String s)
      {
         assertFalse(b);
         assertNull(i);
         assertEquals("replay", s);
         return "test";
      }
   }

   public void testDelegateForNativeMethod()
   {
      new Expectations() {
         @NonStrict Collaborator mock;

         {
            mock.nativeMethod(anyBoolean);
            result = new Delegate() {
               Long nativeMethod(boolean b) { assertTrue(b); return 0L; }
            };
         }
      };

      assertEquals(0L, new Collaborator().nativeMethod(true));
   }

   public void testDelegateForFinalMethod()
   {
      new Expectations() {
         @NonStrict Collaborator mock;

         {
            mock.finalMethod();
            result = new Delegate() { char finalMethod() { return 'M'; } };
         }
      };

      assertEquals('M', new Collaborator().finalMethod());
   }

   public void testDelegateForPrivateMethod(@NonStrict final Collaborator collaborator)
   {
      new Expectations() {{
         invoke(collaborator, "privateMethod");
         result = new Delegate() { float privateMethod() { return 0.5F; } };
      }};

      assertEquals(0.5F, collaborator.privateMethod(), 0);
   }

   public void testDelegateForMethodWithCompatibleButDistinctParameterType()
   {
      new Expectations() {
         @NonStrict Collaborator collaborator;

         {
            collaborator.addElements(this.<Collection<String>>withNotNull());
            result = new Delegate() {
               void addElements(Collection<String> elements) { elements.add("test"); }
            };
         }
      };

      List<String> elements = new ArrayList<String>();
      new Collaborator().addElements(elements);

      assertTrue(elements.contains("test"));
   }

   public void testDelegateReceivingNullArguments()
   {
      new NonStrictExpectations() {
         Collaborator collaborator;

         {
            collaborator.doSomething(true, null, null);
            returns(new Delegate() {
               void doSomething(boolean b, int[] i, String s) {}
            });
         }
      };

      assertNull(new Collaborator().doSomething(true, null, null));
   }

   public void testDelegateWithTwoMethods(final Collaborator collaborator)
   {
      new NonStrictExpectations() {{
         collaborator.doSomething(true, null, "str");
         result = new Delegate() {
            String someOther() { return ""; }
            void doSomething(boolean b, int[] i, String s) {}
         };
      }};

      assertNull(collaborator.doSomething(true, null, "str"));
   }

   public void testDelegateWithSingleMethodHavingADifferentName()
   {
      new NonStrictExpectations() {
         Collaborator collaborator;

         {
            collaborator.doSomething(true, null, "str");
            result = new Delegate() {
               void onReplay(boolean b, int[] i, String s)
               {
                  assertTrue(b);
                  assertNull(i);
                  assertEquals("str", s);
               }
            };
         }
      };

      assertNull(new Collaborator().doSomething(true, null, "str"));
   }

   public void testDelegateWithSingleMethodHavingNoParameters()
   {
      new NonStrictExpectations() {
         Collaborator collaborator;

         {
            collaborator.doSomething(anyBoolean, null, null);
            result = new Delegate() { String onReplay() { return "action"; } };
         }
      };

      assertEquals("action", new Collaborator().doSomething(true, null, null));
   }

   public void testDelegateWithSingleMethodHavingNoParametersExceptForInvocationContext()
   {
      new NonStrictExpectations() {
         Collaborator collaborator;

         {
            collaborator.doSomething(anyBoolean, null, null);
            result = new Delegate() {
               void doSomething(Invocation inv) { assertEquals(1, inv.getInvocationCount()); }
            };
         }
      };

      assertNull(new Collaborator().doSomething(false, new int[] {1, 2}, "test"));
   }

   public void testDelegateWithOneMethodHavingDifferentParameters(final Collaborator collaborator)
   {
      new NonStrictExpectations() {{
         collaborator.doSomething(true, null, "str");
         result = new Delegate() { void doSomething(boolean b, String s) {} };
      }};

      try {
         assertNull(collaborator.doSomething(true, null, "str"));
         fail();
      }
      catch (IllegalArgumentException e) {
         assertTrue(e.getMessage().contains("doSomething("));
      }
   }

   public void testDelegateWithTwoInvalidMethods(final Collaborator collaborator)
   {
      new NonStrictExpectations() {{
         collaborator.doSomething(true, null, "str");
         result = new Delegate() {
            String someOther() { return ""; }
            void doSomethingElse(boolean b, int[] i, String s) {}
         };
      }};

      try {
         assertNull(collaborator.doSomething(true, null, "str"));
         fail();
      }
      catch (IllegalArgumentException e) {
         assertTrue(e.getMessage().startsWith("No compatible method found"));
      }
   }

   public void testDelegateCausingConcurrentMockInvocation()
   {
      final Collaborator collaborator = new Collaborator();
      final Thread t = new Thread(new Runnable() {
         public void run() { collaborator.doSomething(false, null, ""); }
      });

      new Expectations() {
         @NonStrict Collaborator mock;

         {
            mock.getValue(); times = 1;
            result = new Delegate() {
               int executeInAnotherThread() throws Exception
               {
                  t.start();
                  t.join();
                  return 1;
               }
            };
         }
      };

      assertEquals(1, collaborator.getValue());
   }

   public void testDelegateWhichCallsTheSameMockedMethod(final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.getValue();
         result = new Delegate() {
            int count;
            // Would result in a StackOverflowError without a termination condition.
            int delegate() { return count++ > 1 ? 123 : 1 + mock.getValue(); }
         };
      }};

      assertEquals(125, mock.getValue());
   }

   public void testDelegateWhichCallsAnotherMockedMethod_regularMocking(final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.getValue();
         result = new Delegate() {
            int delegate() { return mock.finalMethod(); }
         };

         mock.finalMethod(); result = 'A';
      }};

      assertEquals('A', mock.getValue());
   }

   public void testDelegateWhichCallsAnotherMockedMethod_dynamicMockingOfClass()
   {
      final Collaborator collaborator = new Collaborator();

      new NonStrictExpectations(Collaborator.class) {{
         Collaborator.staticMethod(); result = false;

         collaborator.getValue();
         result = new Delegate() {
            int delegate() { return Collaborator.staticMethod() ? 1 : collaborator.finalMethod(); }
         };

         collaborator.finalMethod(); result = 'A';
      }};

      assertEquals('A', collaborator.getValue());
   }

   public void testDelegateWhichCallsAnotherMockedMethod_dynamicMockingOfInstance()
   {
      final Collaborator collaborator = new Collaborator();

      new NonStrictExpectations(collaborator) {{
         collaborator.getValue();
         result = new Delegate() {
            int delegate() { return collaborator.finalMethod(); }
         };

         collaborator.finalMethod(); result = 'A';
      }};

      assertEquals('A', collaborator.getValue());
   }

   public void testDelegateWhichCallsAnotherMockedMethod_injectableMocking(@Injectable final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.getValue();
         result = new Delegate() {
            int delegate() { return mock.finalMethod(); }
         };

         mock.finalMethod(); result = 'A';
      }};

      assertEquals('A', mock.getValue());
   }

   public void testDelegateWhichCallsAnotherMockedMethodProducingACascadedInstance(@Cascading final Collaborator mock)
   {
      new NonStrictExpectations() {{
         mock.getFoo().doSomething(); result = 123;

         mock.getValue();
         result = new Delegate() {
            int delegate() { return mock.getFoo().doSomething(); }
         };
      }};

      assertEquals(123, mock.getFoo().doSomething());
      assertEquals(123, mock.getValue());
   }

   public void testDelegateCallingMockedMethodLaterVerified(final Collaborator collaborator, final Runnable action)
   {
      new NonStrictExpectations() {{
         collaborator.getFoo();
         result = new Delegate() {
            void delegate() { action.run(); }
         };
      }};

      collaborator.getFoo();

      new Verifications() {{ action.run(); }};
   }
}
