package br.com.drkmatheus.service;

import br.com.drkmatheus.dao.BankAccountDAO;
import br.com.drkmatheus.dao.BankAccountDAOImpl;
import br.com.drkmatheus.dao.BankTransactionDAO;
import br.com.drkmatheus.entities.BankAccount;
import br.com.drkmatheus.entities.BankTransaction;
import java.math.BigDecimal;
import java.util.List;

public class BankTransactionService {
    private BankTransactionDAO bankTransactionDAO;
    private BankAccountDAO bankAccountDAO;

    public BankTransactionService(BankTransactionDAO bankTransactionDAO, BankAccountDAO bankAccountDAO) {
        this.bankTransactionDAO = bankTransactionDAO;
        this.bankAccountDAO =  bankAccountDAO;
    }

    public void deposit(BankAccount account, BigDecimal amount) {
        // Realiza o depósito
        account.setBalance(account.getBalance().add(amount)); // Atualiza o saldo da conta
        bankAccountDAO.updateAccount(account);
        BankTransaction transaction = new BankTransaction(account, "Depósito", amount);
        bankTransactionDAO.saveTransaction(transaction); // Salva a transação no banco de dados
    }

    public void withdraw(BankAccount account, BigDecimal amount) {
        // realiza saque
        account.withdraw(amount);

        // atualiza conta no BD
        bankAccountDAO.updateAccount(account);

        // cria e salva transacao
        BankTransaction transaction = new BankTransaction(account, "Saque", amount);
        bankTransactionDAO.saveTransaction(transaction);
    }

    public void transfer(BankAccount sourceAccount, BankAccount targetAccount, BigDecimal amount) {
        // Realiza a transferência entre contas
        if (sourceAccount.getBalance().compareTo(amount) >= 0) {
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount)); // Atualiza o saldo da conta de origem
            targetAccount.setBalance(targetAccount.getBalance().add(amount)); // Atualiza o saldo da conta de destino

            // Salva as transações para ambas as contas
            BankTransaction transaction = new BankTransaction(sourceAccount, "Transferência", amount);
            bankTransactionDAO.saveTransaction(transaction); // Transação de débito na conta de origem

            BankTransaction transactionTarget = new BankTransaction(targetAccount, "Transferência", amount);
            bankTransactionDAO.saveTransaction(transactionTarget); // Transação de crédito na conta de destino
        } else {
            throw new IllegalArgumentException("Saldo insuficiente para a transferência");
        }
    }

    public List<BankTransaction> getTransactions(BankAccount account) {
        // Recupera o extrato da conta
        return bankTransactionDAO.getTransactionByAccountId(account.getIdAccount());
    }
}