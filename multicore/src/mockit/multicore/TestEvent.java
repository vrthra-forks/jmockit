/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.io.*;

import static java.lang.System.*;

import org.junit.runner.*;

public enum TestEvent
{
   TestStarted,
   TestFinished,
   TestFailure,
   TestAssumptionFailed,
   TestIgnored,
   NoMoreTests;

   static TestEvent readNext(InputStream input)
   {
      try {
         input.mark(2);
         int firstByte = input.read();
         int secondByte = input.read();

         if (firstByte != 0x0E || secondByte < 0 || secondByte > NoMoreTests.ordinal()) {
            input.reset();
            return null;
         }

         return TestEvent.values()[secondByte];
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   final void send()
   {
      sendHeader();
      out.flush();
   }

   private void sendHeader()
   {
      out.write(0x0E);
      out.write(ordinal());
   }

   final void send(Description description)
   {
      sendHeader();

      if (description.isTest()) {
         sendTestDescription(description);
      }
      else {
         for (Description childDescription : description.getChildren()) {
            out.write(',');
            sendTestDescription(childDescription);
         }
      }

      out.write('\n');
   }

   private void sendTestDescription(Description testDescription)
   {
      out.print(testDescription.getMethodName());
   }
}
