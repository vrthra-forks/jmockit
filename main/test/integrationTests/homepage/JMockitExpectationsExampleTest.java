/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests.homepage;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

public final class JMockitExpectationsExampleTest
{
   // Common mock fields can be declared here, and must be annotated with @Mocked.

   @Test
   public void testDoOperationAbc()
   {
      new Expectations() {
         // This is a mock field; it can optionally be annotated with @Mocked.
         DependencyXyz mock; // a mocked instance is automatically created and assigned

         {
            new DependencyXyz(); // records an expectation on a constructor invocation
            mock.doSomething("test"); result = 123;
            
            // The expectations above are strict, causing the whole dependency to be strictly verified.
            // Therefore, invocations not recorded here will be considered unexpected, causing the test
            // to fail if they occur while exercising the code under test.
            // Non-strict expectations are also supported.
         }
      };

      // In ServiceAbc#doOperationAbc(String s): "new DependencyXyz().doSomething(s);"
      Object result = new ServiceAbc().doOperationAbc("test");

      assertNotNull(result);

      // That all expectations recorded were actually executed in the replay phase is automatically
      // verified at this point, through transparent integration with the JUnit/TestNG test runner.
   }
}
