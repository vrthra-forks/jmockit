/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.logging;

import org.junit.*;
import static org.junit.Assert.*;

import mockit.*;
import mockit.integration.logging.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/slf4j/src/test/java/demo/org/powermock/examples/Slf4jUserTest.java">PowerMock version</a>
 */
@UsingMocksAndStubs(Slf4jMocks.class)
public final class Slf4jUser_JMockit_Test
{
   @Test
   public void assertSlf4jMockingWorks() throws Exception
   {
      Slf4jUser tested = new Slf4jUser();
      assertEquals("sl4j user", tested.getMessage());
   }
}
