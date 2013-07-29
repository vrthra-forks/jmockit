/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests.serviceA;

import integrationTests.serviceB.*;
import static mockit.Mockit.*;
import static org.junit.Assert.*;
import org.junit.*;

import mockit.*;

public final class ServiceATest
{
   @Before
   public void setUp()
   {
      setUpMocks(MockServiceBThatAvoidsStaticInitialization.class);
   }

   @MockClass(realClass = ServiceB.class)
   public static final class MockServiceBThatAvoidsStaticInitialization
   {
      @Mock
      public static void $clinit()
      {
         // Do nothing.
      }
   }

   @Test
   public void serviceBCalledExactlyOnce()
   {
      setUpMocks(MockServiceBForOneInvocation.class);

      boolean result = new ServiceA().doSomethingThatUsesServiceB(2, "test");

      assertTrue(result);
   }

   @MockClass(realClass = ServiceB.class)
   public static class MockServiceBForOneInvocation
   {
      @Mock(invocations = 1)
      public int computeX(int a, int b)
      {
         // Asserts that the received arguments meets the expected values.
         // Equivalent jMock2 expectations: one(mockOfServiceB).computeX(2, 5);
         assertEquals(2, a);
         assertEquals(5, b);

         // Returns the expected result.
         // Equivalent jMock2 expectation: will(returnValue(7));
         return 7;
      }
   }

   @SuppressWarnings({"JUnitTestMethodWithNoAssertions"})
   @Test
   public void serviceBCalledAtLeastTwoTimes()
   {
      setUpMocks(new MockServiceBForTwoInvocations());

      new ServiceA().doSomethingElseUsingServiceB(3);
   }

   @MockClass(realClass = ServiceB.class)
   public static final class MockServiceBForTwoInvocations
   {
      @Mock(minInvocations = 2)
      public int computeX(int a, int b)
      {
         assertTrue(a + b >= 0);
         return 0;
      }
   }

   @Test
   public void serviceBCalledAtLeastOnceAndAtMostThreeTimes()
   {
      setUpMocks(new MockServiceBForOneToThreeInvocations(), MockServiceBHelper.class);

      ServiceA serviceA = new ServiceA();
      serviceA.doSomethingElseUsingServiceB(2);
      String config = serviceA.getConfig();

      assertEquals("test", config);
   }

   @MockClass(realClass = ServiceB.class)
   public static final class MockServiceBForOneToThreeInvocations
   {
      public ServiceB it;

      @Mock(invocations = 1)
      public void $init(String config)
      {
         assertNotNull(it);
         assertEquals("config", config);
      }

      @Mock(minInvocations = 1, maxInvocations = 3)
      public int computeX(int a, int b)
      {
         assertTrue(a + b >= 0);
         assertNotNull(it);
         return a - b;
      }

      @Mock(reentrant = true)
      public String getConfig()
      {
         String config = it.getConfig();
         assertNull(config);
         return "test";
      }
   }

   @MockClass(realClass = ServiceB.Helper.class)
   static class MockServiceBHelper
   {
      @Mock(invocations = 0)
      void $init()
      {
         throw new IllegalStateException("should not be created");
      }
   }

   @Test
   public void beforeAdvice()
   {
      setUpMocks(new OnEntryTracingAspect());

      ServiceB b = new ServiceB("test");

      assertEquals(3, b.computeX(1, 2));
      assertEquals(5, b.computeX(2, 3));
      assertEquals(-10, b.computeX(0, -10));
   }

   @MockClass(realClass = ServiceB.class)
   public static class OnEntryTracingAspect
   {
      public ServiceB it;

      @Mock(reentrant = true)
      public int computeX(int a, int b)
      {
         return it.computeX(a, b);
      }
   }

   @Test
   public void afterAdvice()
   {
      setUpMocks(new OnExitTracingAspect());

      ServiceB b = new ServiceB("test");

      assertEquals(3, b.computeX(1, 2));
      assertEquals(5, b.computeX(2, 3));
      assertEquals(-10, b.computeX(0, -10));
   }

   @MockClass(realClass = ServiceB.class)
   public static class OnExitTracingAspect
   {
      public ServiceB it;

      @Mock(reentrant = true)
      public int computeX(int a, int b)
      {
         Integer x;

         try {
            x = it.computeX(a, b);
            return x;
         }
         finally {
            // Statements to be executed on exit would be here.
            x = a + b;
         }
      }
   }

   @Test
   public void aroundAdvice()
   {
      setUpMocks(new TracingAspect());

      ServiceB b = new ServiceB("test");

      assertEquals(3, b.computeX(1, 2));
      assertEquals(5, b.computeX(2, 3));
      assertEquals(-10, b.computeX(0, -10));
   }

   @MockClass(realClass = ServiceB.class)
   public static class TracingAspect
   {
      public ServiceB it;

      @Mock(reentrant = true)
      public int computeX(int a, int b)
      {
         int x = it.computeX(a, b);
         return x;
      }
   }
}
