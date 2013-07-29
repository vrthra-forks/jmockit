/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class CapturingInstancesTest
{
   public interface Service { int doSomething(); }
   static final class ServiceImpl implements Service { public int doSomething() { return 1; } }

   public static final class TestedUnit
   {
      private final Service service1 = new ServiceImpl();
      private final Service service2 = new Service() { public int doSomething() { return 2; } };
      Observable observable;

      public int businessOperation(final boolean b)
      {
         new Callable()
         {
            public Object call() { throw new IllegalStateException(); }
         }.call();

         observable = new Observable()
         {
            {
               if (b) {
                  throw new IllegalArgumentException();
               }
            }
         };

         return service1.doSomething() + service2.doSomething();
      }
   }

   @Capturing(maxInstances = 2) Service service;

   @Test
   public void captureServiceInstancesCreatedByTestedConstructor()
   {
      Service initialMockService = service;

      new TestedUnit();

      assertNotSame(initialMockService, service);
      assertFalse(service instanceof ServiceImpl);
   }

   @Test
   public void recordStrictExpectationsForNextTwoInstancesToBeCreatedUsingMockFields()
   {
      new Expectations() {
         @Capturing(maxInstances = 1) Service s1;
         @Capturing(maxInstances = 1) Service s2;

         {
            s1.doSomething(); result = 11;
            s2.doSomething(); result = 22;
         }
      };

      assertEquals(11, new ServiceImpl().doSomething());
      assertEquals(22, new ServiceImpl().doSomething());
   }

   @Test
   public void captureAllInternallyCreatedInstances(@Capturing final Callable<?> callable) throws Exception
   {
      new NonStrictExpectations() {
         @Capturing Observable observable;

         {
            service.doSomething(); returns(3, 4);
         }
      };

      TestedUnit unit = new TestedUnit();
      int result = unit.businessOperation(true);
      assertEquals(4, unit.service1.doSomething());
      assertEquals(4, unit.service2.doSomething());

      assertNotNull(unit.observable);
      assertEquals(7, result);

      new Verifications() {{ callable.call(); }};
   }

   @Test
   public void recordStrictExpectationsForNextTwoInstancesToBeCreatedUsingMockParameters(
      @Capturing(maxInstances = 1) final Service s1, @Capturing(maxInstances = 1) final Service s2)
   {
      new Expectations() {{
         s1.doSomething(); result = 11;
         s2.doSomething(); returns(22, 33);
      }};

      assertEquals(11, new ServiceImpl().doSomething());
      ServiceImpl s = new ServiceImpl();
      assertEquals(22, s.doSomething());
      assertEquals(33, s.doSomething());
   }

   @Test
   public void recordExpectationsForNextTwoInstancesToBeCreatedUsingNonStrictMockFields()
   {
      new Expectations() {
         @Capturing(maxInstances = 1) @NonStrict Service s1;
         @Capturing(maxInstances = 1) @NonStrict Service s2;

         {
            s1.doSomething(); result = 11;
            s2.doSomething(); result = 22;
         }
      };

      ServiceImpl s1 = new ServiceImpl();
      ServiceImpl s2 = new ServiceImpl();
      assertEquals(22, s2.doSomething());
      assertEquals(11, s1.doSomething());
      assertEquals(11, s1.doSomething());
      assertEquals(22, s2.doSomething());
      assertEquals(11, s1.doSomething());
   }

   @Test
   public void recordNonStrictExpectationsForNextTwoInstancesToBeCreatedUsingMockFields()
   {
      new NonStrictExpectations() {
         @Capturing(maxInstances = 1) Service s1;
         @Capturing(maxInstances = 1) Service s2;

         {
            s1.doSomething(); result = 11;
            s2.doSomething(); result = 22;
         }
      };

      assertEquals(11, new ServiceImpl().doSomething());
      assertEquals(22, new ServiceImpl().doSomething());
   }

   @Test
   public void recordExpectationsForNextTwoInstancesToBeCreatedUsingNonStrictMockParameters(
      @Capturing(maxInstances = 1) @NonStrict final Service s1,
      @Capturing(maxInstances = 1) @NonStrict final Service s2)
   {
      new Expectations() {{
         s2.doSomething(); result = 22;
         s1.doSomething(); result = 11;
      }};

      ServiceImpl cs1 = new ServiceImpl();
      assertEquals(11, cs1.doSomething());
      ServiceImpl cs2 = new ServiceImpl();
      assertEquals(22, cs2.doSomething());
      assertEquals(11, cs1.doSomething());
      assertEquals(22, cs2.doSomething());
   }

   static class Base { boolean doSomething() { return false; } }
   static final class Derived1 extends Base {}
   static final class Derived2 extends Base {}

   @Test
   public void verifyExpectationsOnlyOnOneOfTwoSubclassesForOneCapturedInstance()
   {
      new NonStrictExpectations() {
         @Capturing Derived1 firstCapture;

         {
            new Derived1(); times = 1;
            onInstance(firstCapture).doSomething(); result = true; times = 1;
         }
      };

      assertTrue(new Derived1().doSomething());
      assertFalse(new Derived2().doSomething());
   }

   @Test
   public void verifyExpectationsOnlyOnOneOfTwoSubclassesForAnyNumberOfCapturedInstances()
   {
      new NonStrictExpectations() {
         @Capturing Derived1 anyCapture;

         {
            new Derived1(); minTimes = 1;
            onInstance(anyCapture).doSomething(); result = true; times = 3;
         }
      };

      assertTrue(new Derived1().doSomething());
      assertFalse(new Derived2().doSomething());
      Derived1 d1b = new Derived1();
      assertTrue(d1b.doSomething());
      assertTrue(d1b.doSomething());
   }

   @Test
   public void verifyExpectationsOnlyOnOneOfTwoSubclassesForTwoCapturedInstances()
   {
      new NonStrictExpectations() {
         @Capturing(maxInstances = 1) Derived1 firstCapture;
         @Capturing(maxInstances = 1) Derived1 secondCapture;

         {
            new Derived1(); times = 2;
            onInstance(firstCapture).doSomething(); result = true; times = 1;
            onInstance(secondCapture).doSomething(); result = true; times = 1;
         }
      };

      assertTrue(new Derived1().doSomething());
      assertFalse(new Derived2().doSomething());
      assertTrue(new Derived1().doSomething());
   }
}