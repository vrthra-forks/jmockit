/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package orderMngr.domain.customer;

import javax.persistence.*;

@Entity
public class Customer
{
   @Id
   private String id;

   String firstName;

   String lastName;

   String deliveryAddress;

   public Customer()
   {
   }

   public Customer(String id, String firstName, String lastName, String deliveryAddress)
   {
      this.id = id;
      this.firstName = firstName;
      this.lastName = lastName;
      this.deliveryAddress = deliveryAddress;
   }

   public String getId()
   {
      return id;
   }

   public String getFirstName()
   {
      return firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public String getDeliveryAddress()
   {
      return deliveryAddress;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Customer customer = (Customer) o;

      return id.equals(customer.id);
   }

   @Override
   public int hashCode()
   {
      return id.hashCode();
   }
}
