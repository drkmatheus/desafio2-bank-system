package br.com.drkmatheus.dao;

import br.com.drkmatheus.entities.BankAccount;
import br.com.drkmatheus.entities.BankAccountType;
import br.com.drkmatheus.entities.BankClient;

import java.util.Optional;

public interface BankAccountDAO {
    void save(BankAccount bankAccount);
    BankAccount createAccount(BankClient client, String typeName);
    Optional<BankAccount> findById(int id);
    void updateAccount(BankAccount bankAccount);
}
