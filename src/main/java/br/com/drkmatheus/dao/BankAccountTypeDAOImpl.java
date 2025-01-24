package br.com.drkmatheus.dao;

import br.com.drkmatheus.entities.BankAccountType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class BankAccountTypeDAOImpl implements BankAccountTypeDAO {

    private SessionFactory sessionFactory;

    public BankAccountTypeDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<BankAccountType> findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            BankAccountType bankAccountType = session.get(BankAccountType.class, id);
            return Optional.ofNullable(bankAccountType);
        }
    }

    @Override
    public Optional<BankAccountType> findByTypeName(String typeName) {
        try (Session session = sessionFactory.openSession()) {
            Query<BankAccountType> query = session.createQuery(
                    "FROM BankAccountType WHERE typeName = :typeName", BankAccountType.class
            );
            query.setParameter("typeName", typeName);
            return query.uniqueResultOptional();
        }
        catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void save(BankAccountType bankAccountType) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(bankAccountType);
            session.getTransaction().commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<BankAccountType> findAll() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("FROM BankAccountType", BankAccountType.class).list();
    }
}
