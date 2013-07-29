/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import org.testng.annotations.*;

import mockit.*;
import mockit.internal.annotations.MockStateBetweenTestMethodsJUnit45Test.*;

@Test
@UsingMocksAndStubs(TheMockClass.class)
public final class MockStateBetweenTestMethodsNGTest
{
   public void firstTest()
   {
      TheMockClass.assertMockState(0);
      RealClass.doSomething();
      TheMockClass.assertMockState(1);
   }

   public void secondTest()
   {
      TheMockClass.assertMockState(0);
      RealClass.doSomething();
      TheMockClass.assertMockState(1);
   }
}