package br.com.drkmatheus.entities;
import jakarta.persistence.*;
import java.math.BigDecimal;


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

//    public List<BankTransaction> getBankTransactions() {
//            return transactions;
//    }
//
//    public void setTransactions(List<BankTransaction> transactions) {
//            this.transactions = transactions;
//    }
}
