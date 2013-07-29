/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4;

import org.junit.runner.*;
import org.junit.runners.*;

import mockit.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({MockDependencyTest.class, UseDependencyTest.class})
@UsingMocksAndStubs(TestSuiteWithAnnotation.MockAnotherDependency.class)
public final class TestSuiteWithAnnotation
{
   static final class MockAnotherDependency extends MockUp<AnotherDependency>
   {
      MockAnotherDependency()
      {
         AnotherDependency.mockedAtSuiteLevel = true;
      }

      @Mock static boolean alwaysTrue() { return false; }
   }
}
