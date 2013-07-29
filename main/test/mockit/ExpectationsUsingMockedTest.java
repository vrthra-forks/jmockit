/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;

import javax.swing.*;

import static org.junit.Assert.*;
import org.junit.*;

public final class ExpectationsUsingMockedTest
{
   public interface Dependency { String doSomething(boolean b); }

   static class Collaborator
   {
      private int value;

      Collaborator() {}
      Collaborator(int value) { this.value = value; }

      void provideSomeService() {}

      int getValue() { return value; }

      @SuppressWarnings("UnusedDeclaration")
      final void simpleOperation(int a, String b, Date c) {}
   }

   public abstract static class AbstractBase { protected abstract boolean add(Integer i); }
   @NonStrict AbstractBase base;

   static final class DependencyImpl implements Dependency { public String doSomething(boolean b) { return ""; } }
   @Mocked("do.*") DependencyImpl mockDependency;

   @Test
   public void annotatedField()
   {
      new Expectations() {
         @Mocked
         private Collaborator mock;

         {
            new Collaborator().getValue();
         }
      };

      new Collaborator().getValue();
   }

   @Test
   public void annotatedMockFieldWithFilters()
   {
      new Expectations() {
         @Mocked({"(int)", "doInternal()", "[gs]etValue", "complexOperation(Object)"})
         Collaborator mock;

         {
            mock.getValue();
         }
      };

      // Calls the real method, not a mock.
      Collaborator collaborator = new Collaborator();
      collaborator.provideSomeService();

      // Calls the mock method.
      collaborator.getValue();
   }

   @Test
   public void annotatedMockFieldWithInverseFilters()
   {
      new Expectations() {
         @Mocked(
            inverse = true,
            methods = {"(int)", "simpleOperation(int, String, java.util.Date)", "setValue(long)"})
         Collaborator mock;

         {
            mock.provideSomeService();
         }
      };

      Collaborator collaborator = new Collaborator(2);
      collaborator.simpleOperation(1, "", null); // calls real method
      collaborator.provideSomeService(); // calls the mock
   }

   @Test(expected = IllegalArgumentException.class)
   public void annotatedFieldWithInvalidFilter()
   {
      new Expectations() {
         @Mocked("setValue(int")
         Collaborator mock;
      };
   }

   @Test
   public void annotatedParameter(@Mocked final List<Integer> mock)
   {
      new Expectations() {{ mock.get(1); }};

      assertNull(mock.get(1));
   }

   @Test
   public void annotatedFieldAndParameter(@NonStrict final Dependency dependency1)
   {
      new Expectations() {
         @NonStrict private Dependency dependency2;

         {
            dependency1.doSomething(true); result = "1";
            dependency2.doSomething(false); result = "2";
         }
      };

      assertEquals("1", dependency1.doSomething(true));
      assertNull(dependency1.doSomething(false));
   }

   @Test(expected = IllegalArgumentException.class)
   public void mockFinalFieldOfInterfaceType()
   {
      new NonStrictExpectations() { @Mocked final Dependency mock = null; };
   }

   @Test
   public void mockFieldForAbstractClass()
   {
      new Expectations() {{
         base.add(1); result = true;
      }};

      assertFalse(base.add(0));
      assertTrue(base.add(1));
      assertFalse(base.add(2));
   }

   @Test
   public void partialMockingOfConcreteClassThatExcludesConstructors()
   {
      new Expectations() {{
         mockDependency.doSomething(anyBoolean); minTimes = 2;
      }};

      mockDependency.doSomething(true);
      mockDependency.doSomething(false);
      mockDependency.doSomething(true);
   }

   @Test
   public void mockNothingAndStubNoStaticInitializers(@Mocked("") JComponent container)
   {
      assertEquals("Test", new JLabel("Test").getText());
   }

   static class ClassWithStaticInitializer
   {
      static boolean initialized = true;
      static int initialized() { return initialized ? 1 : -1; }
   }

   @Test
   public void onlyStubOutStaticInitializers()
   {
      new Expectations() {
         @Mocked("<clinit>") final ClassWithStaticInitializer unused = null;
      };

      assertEquals(-1, ClassWithStaticInitializer.initialized());
   }

   static class ClassWithStaticInitializer2
   {
      static boolean initialized = true;
      static int initialized() { return initialized ? 1 : -1; }
   }

   @Test
   public void stubOutStaticInitializersWhenSpecified(
      @Mocked(stubOutClassInitialization = true) ClassWithStaticInitializer2 unused)
   {
      assertEquals(0, ClassWithStaticInitializer2.initialized());
      assertFalse(ClassWithStaticInitializer2.initialized);
   }

   static class ClassWithStaticInitializer3
   {
      static boolean initialized = true;
      static int initialized() { return initialized ? 1 : -1; }
   }

   @Test
   public void doNotStubOutStaticInitializersByDefault(@Mocked ClassWithStaticInitializer3 unused)
   {
      assertEquals(0, ClassWithStaticInitializer3.initialized());
      assertTrue(ClassWithStaticInitializer3.initialized);
   }

   static class AnotherClassWithStaticInitializer
   {
      static boolean initialized = true;
      static int initialized() { return initialized ? 1 : -1; }
   }

   @Test
   public void mockEverythingWithoutStubbingStaticInitializers()
   {
      new Expectations() {
         @Mocked(methods = "<clinit>", inverse = true, stubOutClassInitialization = true)
         final AnotherClassWithStaticInitializer unused = null;
      };

      assertEquals(0, AnotherClassWithStaticInitializer.initialized());
      assertTrue(AnotherClassWithStaticInitializer.initialized);
   }

   static class AnotherClassWithStaticInitializer2
   {
      static boolean initialized = true;
      static int initialized() { return initialized ? 1 : -1; }
   }

   @Test
   public void avoidStubbingStaticInitializersThroughSpecificAnnotationAttribute(
      @Mocked(stubOutClassInitialization = false) AnotherClassWithStaticInitializer2 unused)
   {
      assertEquals(0, AnotherClassWithStaticInitializer2.initialized());
      assertTrue(AnotherClassWithStaticInitializer2.initialized);
   }

   class InnerClass { int getValue() { return -1; } }

   @Test
   public void mockInnerClass(final InnerClass innerMock)
   {
      assertEquals(0, innerMock.getValue());

      new NonStrictExpectations() {{
         innerMock.getValue(); result = 123; times = 1;
      }};

      assertEquals(123, new InnerClass().getValue());
   }

   static final class ClassWithNative
   {
      int doSomething() { return nativeMethod(); }
      private native int nativeMethod();
   }

   @Test
   public void partiallyMockNativeMethod(@Mocked("nativeMethod") final ClassWithNative mock)
   {
      new Expectations() {{
         mock.nativeMethod(); result = 123;
      }};

      assertEquals(123, mock.doSomething());
   }
}
