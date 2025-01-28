package br.com.drkmatheus.dao;

import br.com.drkmatheus.config.HibernateUtil;
import br.com.drkmatheus.entities.BankAccount;
import br.com.drkmatheus.entities.BankAccountType;
import br.com.drkmatheus.entities.BankClient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.Optional;

public class BankAccountDAOImpl implements BankAccountDAO {
    private final SessionFactory sessionFactory;
    private final BankAccountTypeDAO bankAccountTypeDAO;

    public BankAccountDAOImpl(SessionFactory sessionFactory, BankAccountTypeDAO bankAccountTypeDAO) {
        this.sessionFactory = sessionFactory;
        this.bankAccountTypeDAO = bankAccountTypeDAO;
    }

    @Override
    public void save(BankAccount bankAccount) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(bankAccount);
            session.getTransaction().commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(BankAccount bankAccount) {
        // Obtém a sessão atual do Hibernate
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            // Inicia a transação
            transaction = session.beginTransaction();

            // Exclui a conta bancária, utilizando o método delete do Hibernate
            session.delete(bankAccount);

            // Comita a transação
            transaction.commit();
        } catch (Exception e) {
            // Caso ocorra algum erro, faz o rollback da transação
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @Override
    public BankAccount createAccount(BankClient client, String typeName) {
        try (Session session = sessionFactory.openSession()) {
            // procura tipo de conta
            Optional<BankAccountType> bankAccountType = Optional.ofNullable(bankAccountTypeDAO.findByTypeName(typeName)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Account Type.")));

            // criar nova conta
            BankAccount newBankAccount = new BankAccount();
            newBankAccount.setClient(client);
            newBankAccount.setAccountType(bankAccountType.get());
            newBankAccount.setBalance(BigDecimal.ZERO);

            // salvar a conta
            save(newBankAccount);

            return newBankAccount;
        }
    }

    @Override
    public Optional<BankAccount> findById(int id) {
            try (Session session = sessionFactory.openSession()) {
                return Optional.ofNullable(session.get(BankAccount.class, id));
            }
    }

    @Override
    public void updateAccount(BankAccount bankAccount) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.update(bankAccount);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error in updating account.", e);
        }
    }

    @Override
    public void deactivateAccount(int accountId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                // desativa conta
                BankAccount account = session.get(BankAccount.class, accountId);
                account.deactivate();

                // desativa cliente
                BankClient client = account.getClient();
                client.deactivate();

                // atualiza na memoria
                session.update(account);
                session.update(client);

                // atualiza no bd

                transaction.commit();

            }
            catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }
}
