package br.com.drkmatheus.dao;

import br.com.drkmatheus.entities.BankTransaction;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class BankTransactionDAOImpl implements BankTransactionDAO {
    private Session session;

    public BankTransactionDAOImpl(Session session) {
        this.session = session;
    }

    @Override
    public void saveTransaction(BankTransaction transaction) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(transaction);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error on saving transaction.", e);
        }
    }

    @Override
    public List<BankTransaction> getTransactionByAccountId(int accountId) {
        return session.createQuery("FROM BankTransaction WHERE bankAccount.id = :accountId ORDER BY transactionDate DESC", BankTransaction.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }
}
