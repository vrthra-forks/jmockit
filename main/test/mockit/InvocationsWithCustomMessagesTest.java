/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;

import static org.junit.Assert.*;

import mockit.internal.*;

public final class InvocationsWithCustomMessagesTest
{
   static final String message = "custom message";
   @Mocked Collaborator mock;

   static class Collaborator
   {
      private int value;

      @SuppressWarnings("RedundantNoArgConstructor")
      Collaborator() {}

      private static String doInternal() { return "123"; }

      void provideSomeService() {}

      int getValue() { return value; }
      void setValue(int value) { this.value = value; }
   }

   @Test(expected = IllegalStateException.class)
   public void attemptToSpecifyErrorMessageWithNoExpectationRecorded()
   {
      new Expectations() {{ $ = "error"; }};
   }

   @Test(expected = MissingInvocation.class)
   public void replayWithUnexpectedInvocation()
   {
      new Expectations() {{
         mock.getValue(); $ = message;
      }};

      try {
         mock.provideSomeService();
      }
      catch (UnexpectedInvocation e) {
         if (e.getMessage().startsWith(message)) {
            return;
         }
      }

      throw new IllegalStateException("should not get here");
   }

   @Test(expected = MissingInvocation.class)
   public void replayStrictExpectationOnceMoreThanExpected()
   {
      new Expectations() {{
         Collaborator.doInternal();
         mock.provideSomeService(); minTimes = 1; $ = message; maxTimes = 2;
      }};

      Collaborator.doInternal();

      try {
         Collaborator.doInternal();
      }
      catch (UnexpectedInvocation e) {
         if (e.getMessage().startsWith(message)) {
            return;
         }
      }

      throw new IllegalStateException("should not get here");
   }

   @Test
   public void replayNonStrictExpectationOnceMoreThanExpected()
   {
      new NonStrictExpectations() {{
         new Collaborator(); times = 1; $ = message;
      }};

      new Collaborator();

      try {
         new Collaborator();
      }
      catch (UnexpectedInvocation e) {
         assertTrue(e.getMessage().startsWith(message));
         return;
      }

      fail();
   }

   @Test(expected = MissingInvocation.class)
   public void replayWithMissingNonStrictExpectation()
   {
      new NonStrictExpectations() {{
         new Collaborator(); minTimes = 2; maxTimes = 3; $ = message;
      }};

      new Collaborator();
   }

   @Test(expected = MissingInvocation.class)
   public void replayWithMissingExpectedInvocation()
   {
      new Expectations() {{
         mock.setValue(123); $ = message;
      }};
   }

   @Test(expected = IllegalStateException.class)
   public void attemptToSpecifyErrorMessageWithNoExpectationVerified()
   {
      new Verifications() {{ $ = "error"; }};
   }

   @Test
   public void verifyInvocationThatDidNotOccur()
   {
      try {
         new Verifications() {{
            mock.provideSomeService(); times = 1; $ = message;
         }};
      }
      catch (MissingInvocation e) {
         if (!e.getMessage().startsWith(message)) {
            throw new IllegalStateException("Missing custom message prefix", e);
         }
      }
   }

   @Test
   public void verifyMissingInvocationAfterOneThatDidOccur()
   {
      Collaborator.doInternal();
      Collaborator.doInternal();

      try {
         new VerificationsInOrder() {{
            Collaborator.doInternal();
            mock.provideSomeService(); minTimes = 1; $ = message; maxTimes = 2;
         }};
      }
      catch (MissingInvocation e) {
         if (!e.getMessage().startsWith(message)) {
            throw new IllegalStateException("Missing custom message prefix", e);
         }
      }
   }

   @Test
   public void verifyInvocationThatOccurredOnceMoreThanExpected()
   {
      new Collaborator();
      new Collaborator();

      try {
         new FullVerifications() {{
            new Collaborator();
            times = 1;
            $ = message;
         }};
      }
      catch (UnexpectedInvocation e) {
         assertTrue(e.getMessage().startsWith(message));
      }
   }

   @Test
   public void verifyUnexpectedInvocation()
   {
      mock.provideSomeService();
      mock.setValue(123);

      try {
         new FullVerificationsInOrder() {{
            mock.provideSomeService();
            mock.setValue(anyInt); times = 0; $ = message;
         }};
      }
      catch (UnexpectedInvocation e) {
         if (!e.getMessage().startsWith(message)) {
            throw new IllegalStateException("Missing custom message prefix", e);
         }
      }
   }
}