/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jmock.samples;

import org.junit.*;

import mockit.*;

public final class Publisher_JMockit_Test
{
   @Test
   public void oneSubscriberReceivesAMessage()
   {
      // set up
      final Publisher publisher = new Publisher();
      final String message = "message";

      // expectations
      new Expectations() {
         Subscriber subscriber;

         {
            publisher.add(subscriber);
            subscriber.receive(message);
         }
      };

      // execute
      publisher.publish(message);
   }
}
