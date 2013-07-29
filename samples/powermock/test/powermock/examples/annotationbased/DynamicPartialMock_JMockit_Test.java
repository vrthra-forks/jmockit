/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.annotationbased;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;
import powermock.examples.annotationbased.dao.*;

public final class DynamicPartialMock_JMockit_Test
{
   @Injectable SomeDao someDaoMock;
   @Tested SomeService someService;

   @Test
   public void useDynamicPartialMock()
   {
      new Expectations(someDaoMock) {
         // Only invocations recorded here will stay mocked for the replay phase, which begins
         // immediately after exiting this initialization block.
         {
            someDaoMock.getSomeData();
         }
      };

      assertNull(someService.getData());
      assertNotNull(someService.getMoreData());
   }
}
