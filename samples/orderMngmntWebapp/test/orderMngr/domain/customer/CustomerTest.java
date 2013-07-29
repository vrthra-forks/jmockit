/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package orderMngr.domain.customer;

import java.util.*;

import org.junit.*;

import orderMngr.service.domain.*;
import static orderMngr.service.persistence.Persistence.*;
import static org.junit.Assert.*;

/**
 * These are integration tests which make use of the Hibernate 3 API.
 * They can be executed with the real thing and access a real database, or with the <strong>Hibernate Emulation</strong>
 * tool, which contains an <em>external</em> mock class providing a <em>fake implementation</em> of the Hibernate 3 API.
 * <p/>
 * To use the emulator, the usual "-javaagent" JVM argument must be specified as
 * {@code -javaagent:jmockit.jar=hibernate} (inserting the correct path to jmockit.jar if necessary).
 * Alternatively, when using a HotSpot/JRockit JDK 1.6+ on Windows or Linux, if {@code jmockit-hibernate3emul.jar} is in
 * the classpath then the emulator will be used automatically, without the need for any JVM initialization parameter.
 * <p/>
 * If run without the emulator, the tests should still pass, as long as an appropriate Hibernate session factory and the
 * corresponding relational database are available.
 * The use of emulation allows the tests to run "in memory", without any access to the real Hibernate implementation,
 * and therefore to any real database.
 * <p/>
 * All access to the Hibernate API here and in {@link CustomerManager} goes through the
 * {@link orderMngr.service.persistence.Persistence} static facade, which is only a convenience class.
 * Once the emulator is in use, the Hibernate API can be used from anywhere; all calls to it are transparently
 * redirected to the fake implementation.
 */
public final class CustomerTest extends DomainTest
{
   private final CustomerManager manager = new CustomerManager();

   @Test
   public void findCustomerById()
   {
      Customer customer = newCustomer();
      String id = customer.getId();

      Customer found = manager.findById(id);

      assertPersisted(id, found);
      assertEquals(customer.getFirstName(), found.getFirstName());
      assertEquals(customer.getLastName(), found.getLastName());
      assertEquals(customer.getDeliveryAddress(), found.getDeliveryAddress());
   }

   private Customer newCustomer()
   {
      Customer customer = newCustomerData();
      persist(customer);
      return customer;
   }

   private Customer newCustomerData()
   {
      return new Customer("C01", "John", "Smith", "123 Fake Street");
   }

   @Test
   public void createCustomer() throws Exception
   {
      Customer data = newCustomerData();

      manager.create(data);

      assertPersisted(data.getId(), data);
   }

   @Test(expected = MissingEntityId.class)
   public void createCustomerWithMissingId() throws Exception
   {
      Customer data = new Customer(null, "John", "Smith", "123 Fake Street");
      manager.create(data);
   }

   @Test(expected = MissingRequiredData.class)
   public void createCustomerWithMissingName() throws Exception
   {
      Customer data = new Customer("GH", "", null, "123 Fake Street");
      manager.create(data);
   }

   @Test(expected = DuplicateCustomer.class)
   public void createCustomerWithDuplicateName() throws Exception
   {
      Customer customer = newCustomer();
      Customer data =
         new Customer(
            customer.getId() + 'X', customer.getFirstName(), customer.getLastName(), null);

      manager.create(data);
   }

   @Test
   public void editCustomer() throws Exception
   {
      Customer customer = newCustomer();
      String newAddress = "456 Another Street";

      manager.changeNameAndAddress(customer, "John", "Smith", newAddress);

      assertUpdated(customer);
      assertEquals(newAddress, customer.getDeliveryAddress());
   }

   @Test(expected = DuplicateCustomer.class)
   public void editCustomerWithDuplicateNewName() throws Exception
   {
      Customer customer1 = newCustomer();
      Customer customer2 = new Customer("XYZ", "Another", "Name", null);
      persist(customer2);

      manager.changeNameAndAddress(
         customer2, customer1.getFirstName(), customer1.getLastName(), "");
   }

   @Test
   public void removeCustomer()
   {
      Customer customer = newCustomer();

      manager.remove(customer);

      assertDeleted(customer);
   }

   @Test
   public void findCustomersByName()
   {
      List<Customer> found = manager.findByName("none");
      assertTrue("found when shouldn't", found.isEmpty());

      Customer customer = newCustomer();
      assertFoundByName(customer, customer.getLastName(), "last");
      assertFoundByName(customer, customer.getFirstName(), "first");
      assertFoundByName(customer, customer.getFirstName() + ' ' + customer.getLastName(), "full");
      assertFoundByName(customer, "", "any");
   }
   
   private void assertFoundByName(Customer customer, String fullOrPartialName, String nameDesc)
   {
      List<Customer> found = manager.findByName(fullOrPartialName);
      assertTrue("not found by " + nameDesc + " name", found.contains(customer));
   }
}
