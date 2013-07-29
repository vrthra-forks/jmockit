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
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/jcl/src/test/java/org/powermock/examples/JclUserTest.java">PowerMock version</a>
 */
@UsingMocksAndStubs(CommonsLoggingMocks.class)
public final class JclUser_JMockit_Test
{
   @Test
   public void assertJclMockingWorks()
   {
      JclUser tested = new JclUser();
      assertEquals("jcl user", tested.getMessage());
   }
}
