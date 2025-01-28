package br.com.drkmatheus.service;

import br.com.drkmatheus.config.HibernateUtil;
import br.com.drkmatheus.dao.BankAccountDAO;
import br.com.drkmatheus.dao.BankAccountTypeDAO;
import br.com.drkmatheus.dao.BankClientDAO;
import br.com.drkmatheus.dao.BankClientDAOImpl;
import br.com.drkmatheus.entities.BankAccount;
import br.com.drkmatheus.entities.BankAccountType;
import br.com.drkmatheus.entities.BankClient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.Set;


public class BankClientService {
    private  BankClientDAO bankClientDAO;
    private  BankAccountDAO bankAccountDAO;
    private  BankAccountTypeDAO bankAccountTypeDAO;
    //private final BCryptPasswordEncoder passwordEncoder;

    // Construtor que recebe SessionFactory
    public BankClientService(SessionFactory sessionFactory) {
        this.bankClientDAO = new BankClientDAOImpl(sessionFactory);
        //this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public BankClientService(BankClientDAO bankClientDAO) {
        this.bankClientDAO = bankClientDAO;
        //this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public BankClientService(BankClientDAO bankClientDAO, BankAccountDAO bankAccountDAO, BankAccountTypeDAO bankAccountTypeDAO) {
        this.bankClientDAO = bankClientDAO;
        this.bankAccountDAO = bankAccountDAO;
        this.bankAccountTypeDAO = bankAccountTypeDAO;
    }

    public void addAccountType(BankClient bankClient, int accountType) {
        // Busca o tipo de conta no bd
        BankAccountType bankAccountType = bankAccountTypeDAO.findById(accountType).orElseThrow(() -> new IllegalArgumentException("Non-existent account type"));

        // pega a conta existente do cliente
        BankAccount contaExistente = bankClient.getBankAccounts().get(0);

        // verifica se cliente ja tem esse tipo de conta
        Set<Integer> accountTypeIds = contaExistente.getAccountTypeIds();
        if (accountTypeIds.contains(accountType)) {
            throw new IllegalArgumentException("You already have such an account.");
        }
        // adiciona o novo tipo de conta a lista de tipos de conta
        accountTypeIds.add(accountType);
        contaExistente.setAccountTypeIds(accountTypeIds);

        // atualiza a conta no bd
        bankAccountDAO.updateAccount(contaExistente);
        if (bankClient.getBankAccounts().stream().anyMatch(account -> account.getAccountType().getId() == accountType)) {
            throw new IllegalArgumentException("You already have such an account.");
        }

        System.out.println("Account type " + bankAccountType.getTypeName() + " added successfully!");
    }

    public void removeAccountType(BankClient bankClient, int accountType) {
        // Busca o tipo de conta no bd
        BankAccountType bankAccountType = bankAccountTypeDAO.findById(accountType)
                .orElseThrow(() -> new IllegalArgumentException("Non-existent account type"));

        // pega a conta existente do cliente
        BankAccount contaExistente = bankClient.getBankAccounts().get(0);

        // verifica se cliente tem esse tipo de conta
        Set<Integer> accountTypeIds = contaExistente.getAccountTypeIds();
        if (!accountTypeIds.contains(accountType)) {
            throw new IllegalArgumentException("You do not have such an account.");
        }

        // verifica se é o único tipo de conta
        if (accountTypeIds.size() == 1) {
            throw new IllegalArgumentException("Cannot remove only account type. Account must have at least one type.");
        }

        // remove o tipo de conta da lista de tipos de conta
        accountTypeIds.remove(accountType);
        contaExistente.setAccountTypeIds(accountTypeIds);

        // atualiza a conta no bd
        bankAccountDAO.updateAccount(contaExistente);

        System.out.println("Account type " + bankAccountType.getTypeName() + " removed with success!");
    }

    public void registerNewClient(BankClient bankClient, String rawPassword, int tipoContaId) {
        Session session = HibernateUtil.openSession();
        Transaction transaction = null;
         try {
             transaction = session.beginTransaction();
             BankAccountType accountType = session.get(BankAccountType.class, tipoContaId);

             if (accountType == null) {
                 throw new IllegalArgumentException("Non-existent type selected");
             }

             BankAccount bankAccount = new BankAccount();
             bankAccount.setAccountType(accountType);
             bankAccount.setClient(bankClient);
             bankAccount.addBankAccount(bankAccount);

             // testa se o cpf ja existe no banco
             if (bankClientDAO.cpfExists(bankClient.getCpf())) {
                 throw new IllegalArgumentException("This CPF is already in use");
             }

             // criptografa senha
             bankClient.setPassword(rawPassword);

             // salva o cliente
             session.save(bankClient);
             session.save(bankAccount);
             transaction.commit();
         }
         catch (Exception e) {
             if (transaction != null) transaction.rollback();
             throw new RuntimeException("Error registering customer and account", e);
         } finally {
             if (session != null && session.isOpen()) {
                 session.close();
             }
         }

    }

    public void reactivateAccount(BankClient bankClient) {
        if (bankClient.isActive()) {
            System.out.println("The account is already active");
        }

        bankClient.setActive(true);
        bankClientDAO.update(bankClient);
    }

    private BankAccountType searchBankAccountTypeById(Session session, String nomeTipoConta) {
        Query query = session.createQuery("FROM BankAccountType WHERE typeName = :type_name", BankAccountType.class);
        query.setParameter("type_name", nomeTipoConta);
        return (BankAccountType) query.uniqueResult();
    }

    // validador de forca da senha
    public boolean isPasswordStrong(String rawPassword) {

        return rawPassword.length() >= 5 &&
               rawPassword.matches(".*[A-Z].*") &&
               rawPassword.matches(".*[a-z].*") &&
               rawPassword.matches(".*\\d.*") &&
               rawPassword.matches(".*[!@#$%^&*()].*");
    }
}
