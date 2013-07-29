/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class MultipleMockedTypesTest
{
   public static class FirstDependency
   {
      public int getValue() { return 1; }
   }

   public static class SecondDependency
   {
      public int getValue() { return 2; }
      public int getDifferentValue() { return 3; }
   }

   public static class TestedUnit
   {
      public boolean validate(FirstDependency first)
      {
         SecondDependency second = new SecondDependency();

         return first.getValue() + second.getValue() <= 0;
      }

      public boolean validateWithDifferentValue(FirstDependency first)
      {
         SecondDependency second = new SecondDependency();

         return first.getValue() + second.getDifferentValue() <= 0;
      }

      public boolean validate(FirstDependency first, SecondDependency second)
      {
         return first.getValue() + second.getValue() <= 0;
      }

      static void doSomethingWithInternallyCreatedImplementations()
      {
         new Observer()
         {
            public void update(Observable o, Object arg) { throw new IllegalStateException(); }
         }.update(null, "event");

         new Callable<String>()
         {
            public String call() { return "tested"; }
         }.call();
      }
   }

   @Mocked FirstDependency mock1;

   @Test
   public void invocationsOnMethodsOfDifferentClassesWithDifferentSignatures()
   {
      new NonStrictExpectations() {
         SecondDependency mock2;

         {
            mock1.getValue(); result = 15;
            mock2.getDifferentValue(); result = -50;
         }
      };

      assertTrue(new TestedUnit().validateWithDifferentValue(mock1));

      new Verifications() {{ mock1.getValue(); }};
   }

   @Test
   public void invocationsOnMethodsOfDifferentClassesButSameSignature(final SecondDependency mock2)
   {
      new NonStrictExpectations() {{
         mock1.getValue(); result = 15;
         mock2.getValue(); result = -50;
      }};

      assertTrue(new TestedUnit().validate(mock1));

      new VerificationsInOrder() {{
         mock1.getValue();
         mock2.getValue();
      }};
   }

   public static final class SubDependencyThatInherits extends SecondDependency {}
   public static final class SubDependencyThatOverrides extends SecondDependency
   {
      @Override
      public int getValue() { return 1; }
   }

   @Test
   public void invocationOnBaseTypeWithReplayOnSubtypeThatInheritsTheInvokedMethod()
   {
      new Expectations() {
         SecondDependency mock2;

         {
            mock1.getValue(); result = 15;
            mock2.getValue(); result = -50;
         }
      };

      assertTrue(new TestedUnit().validate(mock1, new SubDependencyThatInherits()));
   }

   @Test
   public void invocationOnBaseTypeWithReplayOnSubtypeThatOverridesTheInvokedMethod(final SecondDependency mock2)
   {
      new NonStrictExpectations() {{
         mock1.getValue(); result = 15;
         mock2.getValue(); result = -50;
      }};

      // The executed method will be the override, which is not mocked.
      assertFalse(new TestedUnit().validate(mock1, new SubDependencyThatOverrides()));

      new FullVerifications() {{
         mock1.getValue();
         mock2.getValue(); times = 0;
      }};
   }

   @Test
   public void invocationOnBaseTypeWithCapturingOfSubtypeThatInheritsTheInvokedMethod()
   {
      new NonStrictExpectations() {
         @Capturing SecondDependency mock2;

         {
            mock1.getValue(); result = 15;
            mock2.getValue(); result = -50;
         }
      };

      assertTrue(new TestedUnit().validate(mock1, new SubDependencyThatInherits()));

      new Verifications() {{ mock1.getValue(); }};
   }

   @Test
   public void invocationOnBaseTypeWithCapturingOfSubtypeThatOverridesTheInvokedMethod(
      @Capturing final SecondDependency mock2)
   {
      new NonStrictExpectations() {{
         mock1.getValue(); result = 15;
         mock2.getValue(); result = -50;
      }};

      assertTrue(new TestedUnit().validate(mock1, new SubDependencyThatOverrides()));

      new FullVerificationsInOrder() {{
         mock1.getValue();
         mock2.getValue();
      }};
   }

   @Test
   public void invocationsOnCapturedImplementationsOfInterfaces(@Capturing final Callable<String> callable)
      throws Exception
   {
      new NonStrictExpectations() {
         @Capturing(maxInstances = 1) Observer observer;

         {
            observer.update(null, any); times = 1;
         }
      };

      TestedUnit.doSomethingWithInternallyCreatedImplementations();

      new Verifications() {{ callable.call(); }};
   }
}