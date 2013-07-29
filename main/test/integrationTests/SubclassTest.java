/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import static mockit.Mockit.*;
import static org.junit.Assert.*;
import org.junit.*;

import mockit.*;

public final class SubclassTest
{
   private static boolean superClassConstructorCalled;
   private static boolean subClassConstructorCalled;
   private static boolean mockConstructorCalled;

   public static class SuperClass
   {
      final String name;

      public SuperClass(int x, String name)
      {
         this.name = name + x;
         superClassConstructorCalled = true;
      }
   }

   public static class SubClass extends SuperClass
   {
      public SubClass(String name)
      {
         super(name.length(), name);
         subClassConstructorCalled = true;
      }
   }

   @Before
   public void setUp()
   {
      superClassConstructorCalled = false;
      subClassConstructorCalled = false;
      mockConstructorCalled = false;
   }

   @Test
   public void captureSubclassThroughClassfileTransformer()
   {
      new NonStrictExpectations()
      {
         @Capturing
         SuperClass captured;
      };

      new SubClass("capture");

      assertFalse(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
   }

   @Test
   public void captureSubclassThroughRedefinitionOfPreviouslyLoadedClasses()
   {
      new SubClass("");
      assertTrue(superClassConstructorCalled);
      assertTrue(subClassConstructorCalled);
      superClassConstructorCalled = false;
      subClassConstructorCalled = false;

      new NonStrictExpectations()
      {
         @Capturing
         SuperClass captured;
      };

      new SubClass("capture");

      assertFalse(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
   }

   @Test
   public void mockSubclassUsingMockUpClass()
   {
      new MockUp<SubClass>()
      {
         @Mock
         void $init(String name)
         {
            assertNotNull(name);
            mockConstructorCalled = true;
         }
      };

      new SubClass("test");

      assertTrue(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
      assertTrue(mockConstructorCalled);
   }

   @Test
   public void mockSubclassUsingSetUpMocks()
   {
      setUpMocks(SubClassMockWithMockups.class);

      new SubClass("test");

      assertTrue(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
      assertTrue(mockConstructorCalled);
   }

   @MockClass(realClass = SubClass.class)
   public static final class SubClassMockWithMockups
   {
      @Mock(invocations = 1)
      public void $init(String name)
      {
         assertNotNull(name);
         mockConstructorCalled = true;
      }
   }

   @Test
   public void mockSubclassUsingExpectationsWithFirstSuperConstructor()
   {
      new Expectations()
      {
         final SubClass mock = null;

         {
            new SubClass("test");
         }
      };

      new SubClass("test");

      assertFalse(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
   }

   @Test
   public void partiallyMockSubclassFilteringInASingleConstructor()
   {
      new Expectations()
      {
         @Mocked("(String)")
         final SubClass mock = null;

         {
            new SubClass("test");
         }
      };

      new SubClass("test");

      assertTrue(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
   }
}
