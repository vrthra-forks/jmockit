/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.logging;

import java.util.logging.Logger;

import org.junit.*;

import mockit.*;

import org.apache.commons.logging.*;
import static org.junit.Assert.*;
import org.slf4j.*;

@UsingMocksAndStubs(
   {JDKLoggingMocks.class, Log4jMocks.class, CommonsLoggingMocks.class, Slf4jMocks.class})
public final class MoreLoggingIntegrationsTest
{
   @Test
   public void jdkLoggingShouldLogNothing()
   {
      Logger log1 = Logger.getAnonymousLogger();

      log1.entering("testing that logger does nothing", "method");
      log1.exiting("testing that logger does nothing", "method", new Object());
   }

   @Test
   public void log4jShouldLogNothing()
   {
      org.apache.log4j.Logger log = org.apache.log4j.Logger.getRootLogger();

      log.warn("testing that log4j does nothing");
   }

   @Test
   public void commonsLoggingShouldLogNothing()
   {
      Log log1 = LogFactory.getLog("test");

      assertFalse(log1.isTraceEnabled());
      log1.error("testing that log does nothing");
      assertFalse(log1.isDebugEnabled());
   }

   @Test
   public void slf4jShouldLogNothing()
   {
      org.slf4j.Logger log = LoggerFactory.getLogger("test");

      log.info("testing that logger does nothing", 1, "2");
      assertFalse(log.isWarnEnabled());
   }
}