/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package orderMngr.domain.customer;

import java.util.*;

import static orderMngr.service.domain.DomainUtil.*;
import orderMngr.service.domain.*;
import orderMngr.service.persistence.*;
import static orderMngr.service.persistence.Persistence.*;

public final class CustomerManager
{
   public Customer findById(String id)
   {
      return load(Customer.class, id);
   }

   public void create(Customer data) throws MissingEntityId, MissingRequiredData, DuplicateCustomer
   {
      if (data.getId() == null) {
         throw new MissingEntityId();
      }

      validateRequiredData(data.getFirstName(), data.getLastName());
      validateUniqueness("", data.getFirstName(), data.getLastName());

      persist(data);
   }

   private void validateUniqueness(String id, String firstName, String lastName)
      throws DuplicateCustomer
   {
      boolean customerWithSameNameExists = Persistence.exists(
         "from Customer c where c.firstName=? and c.lastName=? and c.id <> ?",
         firstName, lastName, id);

      if (customerWithSameNameExists) {
         throw new DuplicateCustomer();
      }
   }

   public void changeNameAndAddress(Customer customer, String newFirstName, String newLastName,
      String newAddress) throws MissingRequiredData, DuplicateCustomer
   {
      validateRequiredData(newFirstName, newLastName);
      validateUniqueness(customer.getId(), newFirstName, newLastName);
      
      customer.firstName = newFirstName;
      customer.lastName = newLastName;
      customer.deliveryAddress = newAddress;
   }

   public void remove(Customer customer)
   {
      delete(customer);
   }

   public List<Customer> findByName(String fullOrPartialName)
   {
      return find(
         "select c from Customer c where lower(c.firstName || ' ' || c.lastName) like ?",
         "%" + fullOrPartialName.toLowerCase() + "%");
   }
}
