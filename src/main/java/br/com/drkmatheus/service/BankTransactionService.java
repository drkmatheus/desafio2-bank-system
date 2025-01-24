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

    public BigDecimal checkBalance(BankAccount account) {
        return account.getBalance();
    }

    public void transfer(BankAccount originAccount, BankAccount targetAccount, BigDecimal amount) {
        // Realiza a verificacao da conta de origem para a transferência entre contas
        if (originAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar a transferencia.");
        }

        // realiza o saque na conta de origem
        originAccount.withdraw(amount);
        bankAccountDAO.updateAccount(originAccount);

        // deposita o valor na conta de destino
        targetAccount.deposit(amount);
        bankAccountDAO.updateAccount(targetAccount);

        // registra as 2 transacoces
        BankTransaction originTransaction = new BankTransaction(originAccount, "Transferencia (Débito)", amount);
        BankTransaction targetTransaction = new BankTransaction(targetAccount, "Transferencia (Crédito)", amount);

        bankTransactionDAO.saveTransaction(originTransaction);
        bankTransactionDAO.saveTransaction(targetTransaction);

        originAccount.setBalance(originAccount.getBalance().subtract(amount)); // Atualiza o saldo da conta de origem
            targetAccount.setBalance(targetAccount.getBalance().add(amount)); // Atualiza o saldo da conta de destino

            // Salva as transações para ambas as contas
            BankTransaction transaction = new BankTransaction(originAccount, "Transferência", amount);
            bankTransactionDAO.saveTransaction(transaction); // Transação de débito na conta de origem

            BankTransaction transactionTarget = new BankTransaction(targetAccount, "Transferência", amount);
            bankTransactionDAO.saveTransaction(transactionTarget); // Transação de crédito na conta de destino

    }

    public List<BankTransaction> getTransactions(BankAccount account) {
        // Recupera o extrato da conta
        return bankTransactionDAO.getTransactionByAccountId(account.getIdAccount());
    }
}