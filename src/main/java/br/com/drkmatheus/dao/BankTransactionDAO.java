package br.com.drkmatheus.dao;

import br.com.drkmatheus.entities.BankTransaction;

import java.util.List;

public interface BankTransactionDAO {

    void saveTransaction(BankTransaction bankTransaction);

    // recuperar extrato de uma conta
    List<BankTransaction> getTransactionByAccountId(int accountId);
}