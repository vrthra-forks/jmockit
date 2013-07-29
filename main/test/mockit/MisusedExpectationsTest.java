/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;

import static org.junit.Assert.*;

import mockit.internal.*;

public final class MisusedExpectationsTest
{
   @SuppressWarnings("UnusedDeclaration")
   static class Blah
   {
      int value() { return 0; }
      void setValue(int value) {}
      String doSomething(boolean b) { return ""; }
   }

   @Mocked Blah mock;

   @Test
   public void multipleReplayPhasesWithFirstSetOfExpectationsFullyReplayed()
   {
      // First record phase:
      new Expectations() {{
         new Blah().value(); result = 5;
      }};

      // First replay phase:
      assertEquals(5, new Blah().value());

      // Second record phase:
      new Expectations() {{
         mock.value(); result = 6;
         mock.value(); result = 3;
      }};

      // Second replay phase:
      assertEquals(6, mock.value());
      assertEquals(3, mock.value());
   }

   @Test
   public void multipleReplayPhasesWithFirstSetOfExpectationsPartiallyReplayed()
   {
      // First record phase:
      new Expectations() {{
         mock.value(); returns(1, 2);
      }};

      // First replay phase:
      assertEquals(1, mock.value());

      // Second record phase:
      new Expectations() {{
         mock.value(); returns(3, 4);
      }};

      // Second replay phase:
      assertEquals(2, mock.value());
      assertEquals(3, mock.value());
      assertEquals(4, mock.value());
   }

   @Test
   public void recordDuplicateInvocationWithNoArguments()
   {
      new NonStrictExpectations() {{
         mock.value(); result = 1;
         mock.value(); result = 2; // second recording overrides the first
      }};

      assertEquals(2, mock.value());
      assertEquals(2, mock.value());
   }

   @Test
   public void recordDuplicateInvocationWithArgumentMatcher()
   {
      new NonStrictExpectations() {{
         mock.setValue(anyInt); result = new UnknownError();
         mock.setValue(anyInt); // overrides the previous one
      }};

      mock.setValue(3);
   }

   @Test
   public void recordDuplicateInvocationInSeparateNonStrictExpectationBlocks()
   {
      new NonStrictExpectations() {{
         mock.value(); result = 1;
      }};

      new NonStrictExpectations() {{
         mock.value(); result = 2; // overrides the previous expectation
      }};

      assertEquals(2, mock.value());
   }

   @Test(expected = AssertionError.class)
   public void recordSameInvocationInNonStrictExpectationBlockThenInStrictOne()
   {
      new NonStrictExpectations() {{
         mock.value(); result = 1;
      }};

      new Expectations() {{
         // This expectation can never be replayed, so it will cause the test to fail:
         mock.value(); result = 2;
      }};

      assertEquals(1, mock.value());
      assertEquals(1, mock.value());
   }

   @Test
   public void recordNonStrictExpectationAfterInvokingSameMethodInReplayPhase()
   {
      assertEquals(0, mock.value());

      new NonStrictExpectations() {{
         mock.value(); result = 1;
      }};

      assertEquals(1, mock.value());
   }

   @Test
   public void recordStrictExpectationAfterInvokingSameMethodInReplayPhase() throws Exception
   {
      assertEquals(0, mock.value());

      new Expectations() {{
         mock.value(); result = 1;
      }};

      assertEquals(1, mock.value());
   }

   @Test
   public void recordInvocationOnDynamicallyMockedInstanceForClassAlreadyMockedRegularly()
   {
      final Blah blah = new Blah();

      new NonStrictExpectations(blah) {{
         mock.doSomething(true); result = "first";
         blah.value(); result = 123;
         blah.doSomething(true); result = "second";
      }};

      assertEquals("first", mock.doSomething(true));
      assertEquals("second", blah.doSomething(true));
      assertEquals(123, blah.value());
   }

   @Test
   public void recordOrderedInstantiationOfClassMockedTwice()
   {
      new Expectations() {
         Blah mock2;

         {
            // OK because of the strictly ordered matching (will match the *first* invocation with this constructor).
            new Blah();
         }
      };

      new Blah();
   }

   @Test
   public void recordUnorderedInstantiationOfClassMockedTwice(final Blah mock2)
   {
      new NonStrictExpectations() {{
         new Blah(); times = 1;
         mock.value(); result = 123;
         mock2.value(); result = 45;
      }};

      assertEquals(45, mock2.value());
      assertEquals(123, mock.value());
      new Blah();
   }

   @Test
   public void verifyOrderedInstantiationOfClassMockedTwice(final Blah mock2)
   {
      new Blah();
      mock2.doSomething(true);

      new VerificationsInOrder() {{
         new Blah();
         mock2.doSomething(anyBoolean);
      }};
   }

   @Test
   public void verifyUnorderedInstantiationOfClassMockedTwice(final Blah mock2)
   {
      mock.doSomething(false);
      mock2.doSomething(true);
      new Blah();

      new Verifications() {{
         mock2.doSomething(true);
         new Blah();
         mock.doSomething(false);
      }};
   }

   @BeforeClass
   public static void recordExpectationsInStaticContext()
   {
      try {
         new NonStrictExpectations() {
            Blah blah;

            {
               blah.doSomething(anyBoolean); result = "invalid";
            }
         };
      }
      catch (IllegalStateException ignored) {
         // OK
      }
   }

   @SuppressWarnings("UnusedParameters")
   static class BlahBlah extends Blah
   {
      @Override String doSomething(boolean b) { return "overridden"; }
      void doSomethingElse(Object o) {}
      static void doSomethingStatic() {}
   }

   @SuppressWarnings("StaticFieldReferencedViaSubclass")
   @Test
   public void accessSpecialFieldsInExpectationBlockThroughClassQualifierInsteadOfDirectly(final BlahBlah mock)
   {
      new NonStrictExpectations() {{
         mock.value(); Expectations.result = 123; Expectations.minTimes = 1; Expectations.maxTimes = 2;

         mock.doSomething(Expectations.anyBoolean); NonStrictExpectations.result = "test";
         NonStrictExpectations.times = 1;

         mock.setValue(withNotEqual(0));
      }};

      assertEquals(123, mock.value());
      assertEquals("test", mock.doSomething(true));
      mock.setValue(1);
   }

   boolean verified;

   @SuppressWarnings("StaticFieldReferencedViaSubclass")
   @Test
   public void accessSpecialFieldsInVerificationBlockThroughClassQualifierInsteadOfDirectly(final BlahBlah mock)
   {
      assertNull(mock.doSomething(true));
      mock.setValue(1);

      new Verifications() {{
         mock.doSomething(false); Verifications.times = 0;

         mock.doSomethingElse(Expectations.any); FullVerificationsInOrder.maxTimes = 0;

         mock.setValue(FullVerifications.anyInt);
         VerificationsInOrder.forEachInvocation = new Object() {
            void setValue(int v) { assertTrue(v > 0); verified = true; }
         };
      }};

      assertTrue(verified);
   }

   @Test // with Java 7 only: "java.lang.VerifyError: Expecting a stackmap frame ..."
   public void expectationBlockContainingATryBlock()
   {
      new Expectations() {{
         try { mock.doSomething(anyBoolean); } finally { mock.setValue(1); }
      }};

      mock.doSomething(true);
      mock.setValue(1);
   }

   @Test
   public void mixingStrictAndNonStrictExpectationsForSameDynamicallyMockedObject()
   {
      final BlahBlah tested = new BlahBlah();

      new Expectations(tested) {{ tested.value(); }};

      try {
         new NonStrictExpectations(tested) {{ tested.doSomething(anyBoolean); }};
         fail();
      }
      catch (IllegalArgumentException ignore) {}
   }

   @Test
   public void mixingStrictAndNonStrictExpectationsForSameDynamicallyMockedClass()
   {
      new Expectations(BlahBlah.class) {{ BlahBlah.doSomethingStatic(); }};

      try {
         new NonStrictExpectations(BlahBlah.class) {};
         fail();
      }
      catch (IllegalArgumentException ignore) {}
   }

   @Test
   public void mixingStrictAndNonStrictExpectationsForSameDynamicallyMockedClass_forNonStrictBaseClass()
   {
      new Expectations(BlahBlah.class) {{ new Blah(); BlahBlah.doSomethingStatic(); }};

      try {
         new NonStrictExpectations(Blah.class) {};
         fail();
      }
      catch (IllegalArgumentException ignore) {}
   }

   @Test
   public void mixingStrictAndNonStrictExpectationsForSameDynamicallyMockedClass_forStrictBaseClass()
   {
      new Expectations(Blah.class) {{ new Blah(); }};

      try {
         new NonStrictExpectations(BlahBlah.class) {};
         fail();
      }
      catch (IllegalArgumentException ignore) {}
   }

   static class TestedClass
   {
      static int notMocked() { return 1; }
      static int mocked1() { return 2; }
      static int mocked2() { return 5; }
      static int tested() { return notMocked() + mocked1() + mocked2(); }
   }

   @Test(expected = UnexpectedInvocation.class)
   public void partiallyMockClassAndVerifyAllButOneMockedInvocationToStaticMethods()
   {
      new NonStrictExpectations(TestedClass.class) {{
         TestedClass.mocked1(); result = 3;
      }};

      // Not recommended, but still valid since "TestedClass" has been mocked non-strictly:
      new Expectations(TestedClass.class) {{
         TestedClass.mocked2(); result = 7;
      }};

      assertEquals(11, TestedClass.tested());

      new FullVerifications() {{ TestedClass.mocked1(); }};
   }

   class TestedClass2
   {
      int notMocked() { return 1; }
      int mocked1() { return 2; }
      int mocked2() { return 5; }
      int tested() { return notMocked() + mocked1() + mocked2(); }
   }

   @Test(expected = UnexpectedInvocation.class)
   public void partiallyMockClassAndVerifyAllButOneMockedInvocationToInstanceMethods()
   {
      final TestedClass2 tested = new TestedClass2();

      new NonStrictExpectations(TestedClass2.class) {{
         tested.mocked1(); result = 3;
      }};

      // Not recommended, but still valid since "TestedClass" has been mocked non-strictly:
      new Expectations(TestedClass2.class) {{
         tested.mocked2(); result = 7;
      }};

      assertEquals(11, tested.tested());

      new FullVerifications() {{ tested.mocked1(); }};
   }
}
