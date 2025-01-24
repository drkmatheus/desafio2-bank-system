package br.com.drkmatheus.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "bank_transaction")
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int idTransaction;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transaction_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

    @Column(name = "amount")
    private BigDecimal transactionAmount;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount bankAccount;

    public BankTransaction() {
        this.transactionDate = transactionDate;
    }

    public BankTransaction(BankAccount bankAccount, String transactionType, BigDecimal transactionAmount) {
        this.bankAccount = bankAccount;
        this.transactionType = transactionType;
        this.transactionAmount = transactionAmount;
        this.transactionDate = new Date();
    }

    public int getIdTransaction() {
        return idTransaction;
    }

    public void setIdTransaction(int idTransaction) {
        this.idTransaction = idTransaction;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public BankAccount getAccount() {
        return bankAccount;
    }

    public void setAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }
}

