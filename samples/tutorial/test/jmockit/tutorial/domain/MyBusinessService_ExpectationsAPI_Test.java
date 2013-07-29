/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package jmockit.tutorial.domain;

import org.apache.commons.mail.*;

import org.junit.*;

import mockit.*;

import jmockit.tutorial.persistence.*;

public final class MyBusinessService_ExpectationsAPI_Test
{
   @Mocked(stubOutClassInitialization = true) final Database unused = null;
   @NonStrict SimpleEmail email; // calls to setters are irrelevant, so we make it non-strict

   @Test
   public void doBusinessOperationXyz() throws Exception
   {
      final EntityX data = new EntityX(5, "abc", "abc@xpta.net");

      new Expectations() {{
         // "Database" is mocked strictly, therefore the order of these invocations does matter:
         Database.find(withSubstring("select"), any);
         result = new EntityX(1, "AX5", "someone@somewhere.com");

         Database.persist(data);
      }};

      new Expectations() {{
         // Since "email" is a non-strict mock, this invocation can be replayed in any order:
         email.send(); times = 1; // a non-strict invocation requires a constraint if expected
      }};

      new MyBusinessService().doBusinessOperationXyz(data);
   }

   @Test(expected = EmailException.class)
   public void doBusinessOperationXyzWithInvalidEmailAddress() throws Exception
   {
      new NonStrictExpectations() {{
         email.addTo((String) withNotNull()); result = new EmailException();

         // If the e-mail address is invalid, sending the message should not be attempted:
         email.send(); times = 0;
      }};

      EntityX data = new EntityX(5, "abc", "someone@somewhere.com");
      new MyBusinessService().doBusinessOperationXyz(data);
   }
}
