/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package jbossaop.testing.bank;

import junit.framework.*;

import mockit.*;

import jbossaop.testing.customer.*;

/**
 * This class is JMockit's version of the JBoss AOP test from the
 * <a href="http://www.jboss.org/jbossaop/docs/2.0.0.GA/docs/aspect-framework/userguide/en/html/testing.html#testing1">
 * Injecting Mock Objects</a> section in the "Testing with AOP" chapter of the user guide for JBoss AOP 2.
 * <p/>
 * Notice how much simpler this is, when compared to the full code from the JBoss AOP original example.
 * With JMockit, there is no need to use the "Mock Maker" tool, or to write the {@code BankAccountDAOInterceptor} and
 * {@code MockService} classes; no configuration file (such as {@code jboss-aop.xml}) is required, and no build-time
 * bytecode modification tool (such as {@code aopc}) is used.
 */
public final class BankBusinessTest extends TestCase
{
   BankAccount account1;
   BankAccount account2;
   Customer customer;

   @Override
   public void setUp() throws Exception
   {
      account1 = new BankAccount(10);
      account1.setBalance(100);

      account2 = new BankAccount(11);
      account2.setBalance(500);

      customer = new Customer("John", "Doe");
      customer.addAccount(10);
      customer.addAccount(11);
   }

   public void testSumOfAllAccounts()
   {
      new Expectations() {
         // Mock fields (which could also have been annotated with @Mocked):
         @Cascading // causes BankAccountDAOFactory.getBankAccountDAOSerializer() to return a mock
         final BankAccountDAOFactory daoFactory = null; // no instance needed

         // Proxy class created and instantiated by JMockit; this also is the "cascaded mock" that
         // will be returned from calls to BankAccountDAOFactory.getBankAccountDAOSerializer():
         BankAccountDAO dao;

         // Expected method invocations:
         {
            dao.getBankAccount(10); result = account1;
            dao.getBankAccount(11); result = account2;
         }
      };

      double sum = new BankBusiness().getSumOfAllAccounts(customer);

      assertEquals(600, sum, 0);

      // Note that all expected invocations are verified to have actually occurred at this point.
      // This happens because JMockit provides automatic and transparent integration with the JUnit test runner.
   }
}
