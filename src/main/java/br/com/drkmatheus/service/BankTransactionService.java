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
        BankTransaction transaction = new BankTransaction(account, "Deposit", amount);
        bankTransactionDAO.saveTransaction(transaction); // Salva a transação no banco de dados
    }

    public void withdraw(BankAccount account, BigDecimal amount) {
        // realiza saque
        account.withdraw(amount);

        // atualiza conta no BD
        bankAccountDAO.updateAccount(account);

        // cria e salva transacao
        BankTransaction transaction = new BankTransaction(account, "Withdraw", amount);
        bankTransactionDAO.saveTransaction(transaction);
    }

    public BigDecimal checkBalance(BankAccount account) {
        return account.getBalance();
    }

    public void transfer(BankAccount originAccount, BankAccount targetAccount, BigDecimal amount) {
        // verifica se as 2 contas estão ativas
        if (!originAccount.isActive() || !targetAccount.isActive()) {
            throw new IllegalStateException("It is not possible to make transfers with deactivated accounts.");
        }

        // verifica se os 2 clientes estão ativos
        if (!originAccount.getClient().isActive() || !targetAccount.getClient().isActive()) {
            throw new IllegalStateException("It is not possible to make transfers with deactivated clients.");
        }

        // verifica a conta de origem para a transferência
        if (originAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance to make the transfer.");
        }

        // realiza o saque na conta de origem
        originAccount.withdraw(amount);
        bankAccountDAO.updateAccount(originAccount);

        // deposita o valor na conta de destino
        targetAccount.deposit(amount);
        bankAccountDAO.updateAccount(targetAccount);

        // registra as 2 transacoces
        BankTransaction originTransaction = new BankTransaction(originAccount, "Transfer (Debit)", amount);
        BankTransaction targetTransaction = new BankTransaction(targetAccount, "Transfer (Credit)", amount);

        bankTransactionDAO.saveTransaction(originTransaction);
        bankTransactionDAO.saveTransaction(targetTransaction);

    }


    public List<BankTransaction> getTransactions(BankAccount account) {
        // Recupera o extrato da conta
        return bankTransactionDAO.getTransactionByAccountId(account.getIdAccount());
    }
}