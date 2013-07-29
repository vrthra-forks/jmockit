/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.logging;

import java.io.*;
import java.util.logging.*;
import java.util.logging.Logger;

import org.junit.*;

import mockit.*;

import org.apache.commons.logging.*;
import static org.junit.Assert.*;
import org.slf4j.*;
import org.slf4j.helpers.*;

@UsingMocksAndStubs(
   {JDKLoggingMocks.class, Log4jMocks.class, CommonsLoggingMocks.class, Slf4jMocks.class})
public final class LoggingIntegrationsTest
{
   private static PrintStream originalErr;

   @BeforeClass
   public static void redirectSystemOut()
   {
      originalErr = System.err;

      OutputStream testOutput = new OutputStream()
      {
         @Override
         public void write(int b) { fail("Logger wrote output message!"); }
      };

      System.setErr(new PrintStream(testOutput));
   }

   @AfterClass
   public static void restoreSystemErr()
   {
      System.setErr(originalErr);
   }

   @Test
   public void jdkLoggingShouldLogNothing()
   {
      Logger log1 = Logger.getAnonymousLogger();
      Logger log2 = Logger.getAnonymousLogger("bundle");
      Logger log3 = Logger.getLogger(LoggingIntegrationsTest.class.getName());
      Logger log4 = Logger.getLogger(LoggingIntegrationsTest.class.getName(), "bundle");

      assertFalse(log1.isLoggable(Level.ALL));
      log1.severe("testing that logger does nothing");
      log2.setLevel(Level.WARNING);
      log2.info("testing that logger does nothing");
      log3.warning("testing that logger does nothing");
      log4.fine("testing that logger does nothing");
      log4.finest("testing that logger does nothing");
   }

   @Test
   public void log4jShouldLogNothing()
   {
      org.apache.log4j.Logger log1 = org.apache.log4j.Logger.getLogger("test");
      org.apache.log4j.Logger log2 =
         org.apache.log4j.Logger.getLogger(LoggingIntegrationsTest.class);
      org.apache.log4j.Logger log3 = org.apache.log4j.Logger.getLogger("test", null);
      org.apache.log4j.Logger log4 = org.apache.log4j.Logger.getRootLogger();

      //assertFalse(log1.isTraceEnabled());
      log1.error("testing that log4j does nothing");
      log2.setLevel(org.apache.log4j.Level.FATAL);
      log2.debug("testing that log4j does nothing");
      log3.fatal("testing that log4j does nothing");
      log4.info("testing that log4j does nothing");
   }

   @Test
   public void commonsLoggingShouldLogNothing()
   {
      Log log1 = LogFactory.getLog("test");
      Log log2 = LogFactory.getLog(LoggingIntegrationsTest.class);

      assertFalse(log1.isTraceEnabled());
      log1.error("testing that log does nothing");
      assertFalse(log1.isDebugEnabled());
      log2.trace("test");
      log2.debug("testing that log does nothing");
   }

   @Test
   public void slf4jShouldLogNothing()
   {
      org.slf4j.Logger log1 = LoggerFactory.getLogger("test");
      org.slf4j.Logger log2 = LoggerFactory.getLogger(LoggingIntegrationsTest.class);

      assertFalse(log1.isTraceEnabled());
      log1.error("testing that logger does nothing", 1, "2");
      assertFalse(log1.isDebugEnabled());
      log2.trace(new BasicMarkerFactory().getMarker("m"), "test");
      log2.debug("testing that logger does nothing");
   }
}
