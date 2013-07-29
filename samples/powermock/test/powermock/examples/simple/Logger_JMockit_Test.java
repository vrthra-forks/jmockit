/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.simple;

import java.io.*;

import org.junit.*;

import mockit.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/simple/src/test/java/demo/org/powermock/examples/simple/LoggerTest.java">PowerMock version</a>
 */
public final class Logger_JMockit_Test
{
   @Test(expected = IllegalStateException.class)
   public void testException() throws Exception
   {
      new NonStrictExpectations() {
         final FileWriter fileWriter;

         {
            fileWriter = new FileWriter("target/logger.log"); result = new IOException();
         }
      };

      new Logger();
   }

   @Test
   public void testLogger() throws Exception
   {
      new NonStrictExpectations() {
         FileWriter fileWriter;   // can also be final with value "new FileWriter(withAny(""))"
         PrintWriter printWriter; // can also be final with value "new PrintWriter(fileWriter)"

         {
            printWriter.println("qwe");
         }
      };

      Logger logger = new Logger();
      logger.log("qwe");
   }

   @Test
   public void testLogger2(final Logger logger)
   {
      new Expectations(logger) {
         PrintWriter printWriter;

         {
            setField(logger, printWriter);

            printWriter.println("qwe");
         }
      };

      logger.log("qwe");
   }
}
