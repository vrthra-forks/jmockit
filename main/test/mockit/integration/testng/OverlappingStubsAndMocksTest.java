/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.testng;

import org.testng.annotations.*;

import mockit.*;

/**
 * Overlapping mocks/stubbing for the same real class is currently not supported by the Mockups API.
 */
@UsingMocksAndStubs(RealClass.class)
public final class OverlappingStubsAndMocksTest
{
   static final class TheMockClass extends MockUp<RealClass>
   {
      @Mock(invocations = 1)
      void doSomething() {}
   }

   @Test
   public void firstTest()
   {
      new TheMockClass();
      RealClass.doSomething();
   }

   @Test(dependsOnMethods = "firstTest")
   public void secondTest()
   {
      new TheMockClass();
      RealClass.doSomething();
      // With TestNG 6.3.1, this failed with an unexpected final invocation count, caused by duplicate internal state
      // for the "doSomething" mock. The duplication, in turn, was caused by "RealClass" not being restored
      // after the first test, since it was stubbed out for the whole test class.
   }
}

final class RealClass
{
   static void doSomething() { throw new RuntimeException(); }
}
