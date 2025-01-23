package br.com.drkmatheus.dao;

import br.com.drkmatheus.entities.BankAccount;
import br.com.drkmatheus.entities.BankAccountType;
import br.com.drkmatheus.entities.BankClient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

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
    public BankAccount createAccount(BankClient client, String typeName) {
        try (Session session = sessionFactory.openSession()) {
            // procura tipo de conta
            Optional<BankAccountType> bankAccountType = Optional.ofNullable(bankAccountTypeDAO.findByTypeName(typeName)
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de conta inválida")));

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
}
