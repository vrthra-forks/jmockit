/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests.homepage;

import org.junit.*;

import mockit.*;

public final class JMockitVerificationsExampleTest
{
   @Test // notice the "mock parameter", whose argument value will be created automatically
   public void testDoAnotherOperation(final AnotherDependency anotherMock)
   {
      new NonStrictExpectations() {
         DependencyXyz mock; // mock instance created and assigned automatically

         {
            mock.doSomething("test"); result = 123;
         }
      };

      // In ServiceAbc#doAnotherOperationAbc(String s): "new DependencyXyz().doSomething(s);"
      // and "new AnotherDependency().complexOperation(1, obj);".
      new ServiceAbc().doAnotherOperation("test");

      new Verifications() {{
         anotherMock.complexOperation(anyInt, null);
      }};
   }
}
