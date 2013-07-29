package jbossaop.testing.bank;

public class BankAccount
{
   private final long accountNo;
   private int balance;

   public BankAccount(long accountNo)
   {
      this.accountNo = accountNo;
   }

   public long getAccountNo()
   {
      return accountNo;
   }

   public int getBalance()
   {
      return balance;
   }

   public void setBalance(int balance)
   {
      this.balance = balance;
   }
}
