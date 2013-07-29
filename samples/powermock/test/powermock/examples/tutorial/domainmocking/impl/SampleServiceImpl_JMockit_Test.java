/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.tutorial.domainmocking.impl;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;
import powermock.examples.tutorial.domainmocking.*;
import powermock.examples.tutorial.domainmocking.domain.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/tutorial/src/solution/java/demo/org/powermock/examples/tutorial/domainmocking/impl/SampleServiceImplTest.java">PowerMock version</a>
 */
public final class SampleServiceImpl_JMockit_Test
{
   @Tested SampleServiceImpl tested;
   @Injectable PersonService personService;
   @Injectable EventService eventService;

   @Test
   public void testCreatePerson()
   {
      String firstName = "firstName";
      String lastName = "lastName";
      final Person person = new Person(firstName, lastName);

      new Expectations() {
         BusinessMessages businessMessages;

         {
            // All mocks here are strict, so the order of invocation matters:
            new BusinessMessages();
            personService.create(person, withInstanceLike(businessMessages));
            businessMessages.hasErrors(); result = false;
         }
      };

      assertTrue(tested.createPerson(firstName, lastName));
   }

   @Test
   public void testCreatePersonWithBusinessError()
   {
      String firstName = "firstName";
      String lastName = "lastName";
      final Person person = new Person(firstName, lastName);

      new Expectations() {
         // Declared non-strict so that the order of invocation is irrelevant:
         @NonStrict BusinessMessages businessMessages;

         {
            // The following mocks are strict, so the order of invocation for them does matter:
            personService.create(person, withInstanceLike(businessMessages));
            businessMessages.hasErrors(); result = true;
            eventService.sendErrorEvent(person, withInstanceLike(businessMessages));
         }
      };

      assertFalse(tested.createPerson(firstName, lastName));
   }

   // Notice that this test does not in fact need any mocking, but just for demonstration...
   @Test(expected = SampleServiceException.class)
   public void testCreatePersonWithIllegalName()
   {
      final String firstName = "firstName";
      final String lastName = "lastName";

      new Expectations() {
         @Mocked Person person;

         {
            new Person(firstName, lastName); result = new IllegalArgumentException("test");
         }
      };

      tested.createPerson(firstName, lastName);
   }
}
