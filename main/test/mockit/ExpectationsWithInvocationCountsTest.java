/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

import mockit.internal.*;

public final class ExpectationsWithInvocationCountsTest
{
   private final CodeUnderTest codeUnderTest = new CodeUnderTest();

   static class CodeUnderTest
   {
      private final Collaborator dependency = new Collaborator();

      void doSomething()
      {
         dependency.provideSomeService();
      }

      void doSomethingElse()
      {
         dependency.simpleOperation(1, "b", null);
      }
   }

   static class Collaborator
   {
      Collaborator() {}

      @SuppressWarnings("UnusedDeclaration")
      Collaborator(int value) {}

      void provideSomeService() {}

      @SuppressWarnings("UnusedDeclaration")
      final void simpleOperation(int a, String b, Date c) {}
   }

   @Test
   public void expectOnce()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService();
         }
      };

      codeUnderTest.doSomething();
   }

   @Test(expected = UnexpectedInvocation.class)
   public void expectOnceButReplayTwice()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService();
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();

      fail("Should not get here");
   }

   @Test(expected = UnexpectedInvocation.class)
   public void expectOnceButReplayThreeTimes()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService();
         }
      };

      codeUnderTest.doSomething();

      try {
         codeUnderTest.doSomething();
      }
      finally {
         codeUnderTest.doSomething();
      }

      fail("Should not get here");
   }

   @Test
   public void expectTwiceByRepeatingTheExpectation()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService();
            mock.provideSomeService();
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
   }

   @Test
   public void expectTwiceByUsingInvocationCount()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); times = 2;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test(expected = UnexpectedInvocation.class)
   public void expectTwiceByUsingInvocationCountButReplayOnlyOnce()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); times = 2;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test(expected = UnexpectedInvocation.class)
   public void expectExactlyTwiceButReplayMoreTimes()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); times = 2;
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
   }

   @Test
   public void expectAtLeastOnceAndReplayTwice()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 1;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test(expected = MissingInvocation.class)
   public void expectAtLeastTwiceButReplayOnceWithSingleExpectation()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 2;
         }
      };

      codeUnderTest.doSomething();
   }

   @Test(expected = UnexpectedInvocation.class)
   public void expectAtLeastTwiceButReplayOnceWithTwoConsecutiveExpectations()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 2;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test
   public void repeatsAtLeastOverwritingUpperLimit()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 2; minTimes = 1;
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
   }

   @Test
   public void expectAtMostTwiceAndReplayOnce()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 2;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test(expected = UnexpectedInvocation.class)
   public void expectAtMostOnceButReplayTwice()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 1;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test(expected = MissingInvocation.class)
   public void repeatsAtMostDoesNotOverwriteLowerLimit()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 2; maxTimes = 3;
         }
      };

      codeUnderTest.doSomething();
   }

   @Test
   public void expectSameMethodOnceOrTwiceThenOnceButReplayEachExpectationOnlyOnce(
      final Collaborator mock)
   {
      new Expectations() {{
         mock.simpleOperation(1, "", null); minTimes = 1; maxTimes = 2;
         mock.simpleOperation(2, "", null);
      }};

      mock.simpleOperation(1, "", null);
      mock.simpleOperation(2, "", null);
   }

   @Test
   public void expectTwoOrThreeTimes()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 2; maxTimes = 3;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test
   public void expectZeroOrMoreTimesAndReplayTwice()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 0; maxTimes = -1;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test
   public void expectZeroOrMoreTimesAndReplayNone()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 0; maxTimes = -1;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomethingElse();
   }

   @Test(expected = MissingInvocation.class)
   public void expectAtLeastOneInvocationMatchingStrictExpectationButInvokeNone()
   {
      new Expectations() {
         Collaborator a;

         {
            a.provideSomeService(); maxTimes = -1;
         }
      };

      // Do nothing at replay time.
   }

   @Test(expected = MissingInvocation.class)
   public void expectOneOrMoreInvocationsFollowedByAnotherWhichWontOccur_maxTimes()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = -1;
            mock.simpleOperation(1, null, null);
         }
      };

      codeUnderTest.doSomething();
   }

   @Test(expected = MissingInvocation.class)
   public void expectOneOrMoreInvocationsFollowedByAnotherWhichWontOccur_minTimes(final Collaborator mock)
   {
      new Expectations() {{
         mock.simpleOperation(1, anyString, null); minTimes = 1;
         mock.provideSomeService();
      }};

      codeUnderTest.doSomethingElse();
   }
}
