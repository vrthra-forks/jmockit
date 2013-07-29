/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.concurrent.*;

import org.junit.*;

import static org.junit.Assert.*;

@SuppressWarnings("UnusedDeclaration")
public final class VerificationsWithLocalFieldsTest
{
   static class Dependency
   {
      Dependency() {}
      private Dependency(int i) {}

      public void setSomething(int value) {}
      public String setSomethingElse(String value) { return value; }
      public void editABunchMoreStuff() {}
      public boolean notifyBeforeSave() { return true; }
      public void prepare() {}
      public void save() {}

      private void privateMethod() {}
      private static void privateStaticMethod(String s, boolean b) {}
   }

   static final class SubDependency extends Dependency {}

   final Dependency dependency = new Dependency();

   @Test
   public void importLocalMockFromPreviousNonStrictExpectationsBlock()
   {
      new NonStrictExpectations() {
         Dependency mock;

         {
            mock.notifyBeforeSave(); result = true;
         }
      };

      dependency.editABunchMoreStuff();
      assertTrue(dependency.notifyBeforeSave());

      new Verifications() {
         Dependency mock;

         {
            mock.editABunchMoreStuff();
         }
      };
   }

   @Test
   public void importLocalMockFromFinalField()
   {
      new NonStrictExpectations() {
         final Dependency mock = new Dependency(5);
      };

      new Dependency(5).setSomethingElse("test");

      new VerificationsInOrder() {
         Dependency mock;

         {
            new Dependency(5);
            mock.setSomethingElse(anyString); times = 1;
         }
      };
   }

   @Test
   public void importLocalMocksFromPreviousExpectationsBlock()
   {
      Expectations exp = new Expectations() {
         @NonStrict Dependency mockDependency;
         @Mocked("run") @Capturing Runnable runnable;
      };

      dependency.editABunchMoreStuff();
      new Runnable() { public void run() {} }.run();

      new FullVerifications() {
         Runnable runnable;
         Dependency mock;

         {
            runnable.run(); times = 1;
            mock.editABunchMoreStuff();
         }
      };
   }

   @Test
   public void importMocksFromMultipleExpectationBlocks()
   {
      SubDependency subDependency = new SubDependency();

      new NonStrictExpectations() { Dependency mock; };
      new NonStrictExpectations() {
         SubDependency mock;

         {
            mock.notifyBeforeSave(); result = true;
         }
      };

      subDependency.notifyBeforeSave();

      new FullVerificationsInOrder() {
         Dependency mock1;
         SubDependency mock2;

         {
            mock1.editABunchMoreStuff(); times = 0;
            mock2.notifyBeforeSave();
         }
      };
   }

   @Test
   public void importMocksFromExpectationBlocksWhereTheFirstHasNoMockFields(final SubDependency sub)
   {
      new NonStrictExpectations() {{ sub.setSomethingElse(anyString); result = "test"; }};

      new NonStrictExpectations() {
         Dependency mock;

         {
            mock.notifyBeforeSave(); result = true;
         }
      };

      sub.notifyBeforeSave();
      sub.notifyBeforeSave();

      new Verifications() {
         Dependency mock1;
         SubDependency mock2;

         {
            mock1.notifyBeforeSave(); minTimes = 2;
         }
      };
   }

   class ReusableExpectations extends NonStrictExpectations
   {
      Dependency mock;

      {
         mock.notifyBeforeSave(); result = true;
      }
   }

   @Test
   public void importMockFromReusableExpectationBlock()
   {
      new ReusableExpectations() {};

      dependency.notifyBeforeSave();
      dependency.save();

      new VerificationsInOrder() {
         Dependency dep;

         {
            dep.notifyBeforeSave();
            unverifiedInvocations();
         }
      };
   }

   class ReusableVerifications extends Verifications
   {
      Dependency mock;

      {
         mock.save(); maxTimes = 1;
      }
   }

   @Test
   public void importMockInReusableVerificationBlock()
   {
      new ReusableExpectations() {};

      dependency.notifyBeforeSave();
      dependency.save();

      new ReusableVerifications() {};
   }

   @Test
   public void importMockForFieldOfGenericType() throws Exception
   {
      new NonStrictExpectations() {
         Callable<Dependency> mock;

         {
            mock.call(); result = dependency;
         }
      };

      new Verifications() {
         Callable<Dependency> mock;

         {
            mock.call(); minTimes = 0;
         }
      };
   }
}