/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

public final class MockFieldCapturingMaxInstancesTest
{
   public interface Service
   {
      int doSomething();
   }

   static final class ServiceImpl implements Service
   {
      public int doSomething() { return 1; }
   }

   @Capturing Service mock1;

   @Test
   public void mockFieldWithUnlimitedCapturing()
   {
      new Expectations()
      {
         {
            mock1.doSomething(); returns(1, 2, 3);
         }
      };

      Service service1 = new ServiceImpl();
      assertSame(service1, mock1);
      assertEquals(1, service1.doSomething());

      Service service2 = new Service() { public int doSomething() { return -1; } };
      assertSame(service2, mock1);
      assertEquals(2, service2.doSomething());

      Service service3 = new ServiceImpl();
      assertSame(service3, mock1);
      assertEquals(3, service3.doSomething());
   }

   static class BaseClass
   {
      final String str;

      BaseClass() { str = ""; }
      BaseClass(String str) { this.str = str; }
   }

   static class DerivedClass extends BaseClass
   {
      @SuppressWarnings({"UnusedDeclaration"})
      DerivedClass() {}

      DerivedClass(String str) { super(str); }
   }

   @Capturing(maxInstances = 1) BaseClass mock2;

   @Test
   public void mockFieldWithCapturingLimitedToOneInstance()
   {
      assertNotNull(mock2);

      BaseClass service1 = new DerivedClass("test 1");
      assertNull(service1.str);
      assertSame(service1, mock2);

      BaseClass service2 = new BaseClass("test 2");
      assertNull(service2.str);
      assertSame(service1, mock2);
   }

   @Capturing(maxInstances = 1) BaseClass mock3;

   @Test
   public void secondMockFieldWithCapturingLimitedToOneInstance()
   {
      assertNotNull(mock2);

      BaseClass service1 = new DerivedClass("test 1");
      assertNull(service1.str);
      assertSame(service1, mock2);

      assertNotNull(mock3);

      BaseClass service2 = new BaseClass("test 2");
      assertNull(service2.str);
      assertSame(service1, mock2);
      assertSame(service2, mock3);

      BaseClass service3 = new DerivedClass("test 3");
      assertNull(service3.str);
      assertSame(service1, mock2);
      assertSame(service2, mock3);
   }
}