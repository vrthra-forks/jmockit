package jbossaop.testing.customer;

import java.util.*;

public class Customer
{
   private final List<Long> accounts = new ArrayList<Long>();

   public Customer(String firstName, String lastName)
   {
   }

   public List<Long> getAccounts()
   {
      return accounts;
   }

   public void addAccount(long accountNo)
   {
      accounts.add(accountNo);
   }
}
