/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.io.*;
import static java.lang.System.*;

import org.junit.runner.*;
import org.junit.runner.notification.*;

import static mockit.multicore.TestEvent.*;

final class SubProcessEventListener extends RunListener
{
   @Override
   public void testStarted(Description description) { TestStarted.send(description); }

   @Override
   public void testFinished(Description description) { TestFinished.send(description); }

   @Override
   public void testFailure(Failure failure) throws IOException
   {
      TestFailure.send(failure.getDescription());
      sendSerializableObject(failure.getException());
   }

   private void sendSerializableObject(Serializable object) throws IOException
   {
      ObjectOutputStream objectOutput = new ObjectOutputStream(out);
      objectOutput.writeObject(object);
      objectOutput.flush();
   }

   @Override
   public void testAssumptionFailure(Failure failure)
   {
      TestAssumptionFailed.send(failure.getDescription());
      try { sendSerializableObject(failure.getException()); } catch (IOException ignore) {}
   }

   @Override
   public void testIgnored(Description description) { TestIgnored.send(description); }

   @Override
   public void testRunFinished(Result result) { NoMoreTests.send(); }
}
