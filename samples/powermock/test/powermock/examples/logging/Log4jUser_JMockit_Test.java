/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.logging;

import org.junit.*;

import mockit.*;
import mockit.integration.logging.*;

import static org.junit.Assert.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/log4j/src/test/java/demo/org/powermock/examples/Log4jUserTest.java">PowerMock version</a>
 */
@UsingMocksAndStubs(Log4jMocks.class)
public final class Log4jUser_JMockit_Test
{
   @Test
   public void assertThatLog4jMockingWorks()
   {
      final Log4jUser tested = new Log4jUser();
      final String firstMessage = "first message and ";

      new Expectations(tested) {{
         tested.getMessage(); result = firstMessage;
      }};

      String otherMessage = "other message";
      String actual = tested.mergeMessageWith(otherMessage);

      assertEquals(firstMessage + otherMessage, actual);
   }
}
