/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.finalmocking;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/DocumentationExamples/src/test/java/powermock/examples/finalmocking/StateFormatterTest.java">PowerMock version</a>
 */
public final class StateFormatter_JMockit_Test
{
   @Test
   public void testGetFormattedState_actualStateExists(@Mocked final StateHolder stateHolderMock)
   {
      final String expectedState = "state";
      StateFormatter tested = new StateFormatter(stateHolderMock);

      new Expectations() {{ stateHolderMock.getState(); result = expectedState; }};

      String actualState = tested.getFormattedState();

      assertEquals(expectedState, actualState);
   }

   @Test
   public void testGetFormattedState_noStateExists(@Mocked final StateHolder stateHolderMock)
   {
      StateFormatter tested = new StateFormatter(stateHolderMock);

      new Expectations() {{ stateHolderMock.getState(); result = null; }};

      String actualState = tested.getFormattedState();

      assertEquals("State information is missing", actualState);
   }
}
