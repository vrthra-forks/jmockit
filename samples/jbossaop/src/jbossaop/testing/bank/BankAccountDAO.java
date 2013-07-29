package jbossaop.testing.bank;

public interface BankAccountDAO
{
  void saveBankAccount(BankAccount account);

  BankAccount getBankAccount(long accountNo);

  void removeBankAccount(BankAccount account);
}
