package jbossaop.testing.bank;

import jbossaop.testing.customer.*;

public final class BankBusiness
{
   private final BankAccountDAO bankAccountDAO;

   public BankBusiness()
   {
      // Note that this dependency could be obtained by directly instantiating a DAO implementation,
      // and it still could be easily unit tested with JMockit. Therefore, if enabling unit testing
      // was the only reason to have the DAO interfaces and the DAO factories, the application
      // architecture could be significantly simplified by getting rid of them all, and using
      // concrete DAO implementation classes. Or even better, consider NOT using entity-specific
      // DAOs at all, instead simply using JPA or another ORM API directly or (preferentially) from
      // behind a static facade.
      bankAccountDAO = BankAccountDAOFactory.getBankAccountDAOSerializer();
   }

   public double getSumOfAllAccounts(Customer c)
   {
      double sum = 0;

      for (long accountNo : c.getAccounts()) {
         BankAccount a = bankAccountDAO.getBankAccount(accountNo);

         if (a != null) {
            sum += a.getBalance();
         }
      }

      return sum;
   }
}
