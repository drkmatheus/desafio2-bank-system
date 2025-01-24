package br.com.drkmatheus.entities;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "bank_account")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int idAccount;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private BankClient client;

    @ManyToOne
    @JoinColumn(name = "account_type_id", nullable = false)
    private BankAccountType accountType;

    @Column(name = "account_types", nullable = false)
    private String accountTypes; // armazena os tipos de conta como uma string (ex: "1,2,3")


    @Column(name = "balance", precision = 10, scale = 2)
    private BigDecimal balance;

//    @OneToMany(mappedBy = "account")
//    private List<BankTransaction> transactions;

    public BankAccount() {
        this.balance = BigDecimal.ZERO;
    }

    public BankAccount(int idAccount, BankAccountType accountType, BigDecimal balance, BankClient client) {
            this.idAccount = idAccount;
            this.accountType = accountType;
            this.balance = BigDecimal.ZERO;
            this.client = client;
    }

    public int getIdAccount() {
            return idAccount;
    }

    public void setIdAccount(int idAccount) {
            this.idAccount = idAccount;
    }

    public BankAccountType getAccountType() {
            return accountType;
    }

    public void setAccountType(BankAccountType accountType) {
            this.accountType = accountType;
            this.accountTypes = String.valueOf(accountType.getId());
    }

    public BigDecimal getBalance() {
            return balance;
    }

    public void setBalance(BigDecimal balance) {
            this.balance = balance;
    }

    public BankClient getClient() {
            return client;
    }

    public void setClient(BankClient client) {
            this.client = client;
    }

    public void addBankAccount(BankAccount bankAccount) {
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar saque.");
        }
        this.balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero");
        }
        this.balance = balance.add(amount);
    }

    public Set<Integer> getAccountTypeIds() {
        if (accountTypes == null || accountTypes.isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(accountTypes.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    public void setAccountTypeIds(Set<Integer> accountTypeIds) {
        this.accountTypes = accountTypeIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

//    public List<BankTransaction> getBankTransactions() {
//            return transactions;
//    }
//
//    public void setTransactions(List<BankTransaction> transactions) {
//            this.transactions = transactions;
//    }
}
