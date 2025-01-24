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


    public void listarTiposDeContaVinculados(BankClient cliente) {
        if (cliente.getBankAccounts().isEmpty()) {
            System.out.println("Você não possui nenhuma conta vinculada.");
        } else {
            System.out.println("Tipos de conta vinculados:");
            for (BankAccount conta : cliente.getBankAccounts()) {
                System.out.println("- " + conta.getAccountType().getTypeName());
            }
        }
    }

    public void addAccountType(BankClient bankClient, int accountType) {
        // Busca o tipo de conta no bd
        BankAccountType bankAccountType = bankAccountTypeDAO.findById(accountType).orElseThrow(() -> new IllegalArgumentException("Tipo de conta inexistente"));

        // verifica se cliente ja tem esse tipo de conta
        if (bankClient.getBankAccounts().stream().anyMatch(account -> account.getAccountType().getId() == accountType)) {
            throw new IllegalArgumentException("Você já possui uma conta desse tipo.");
        }

        // cria nova conta associada ao tipo de conta
        BankAccount newAccount = new BankAccount();
        newAccount.setClient(bankClient);
        newAccount.setAccountType(bankAccountType);
        newAccount.setBalance(BigDecimal.ZERO);

        // salva nova conta no bd
        bankAccountDAO.save(newAccount);

        System.out.println("Conta do tipo " + bankAccountType.getTypeName() + " adicionada com sucesso!");
    }

    public void registerNewClient(BankClient bankClient, String rawPassword, int tipoContaId) {
        Session session = HibernateUtil.openSession();
        Transaction transaction = null;
         try {
             transaction = session.beginTransaction();
             BankAccountType accountType = session.get(BankAccountType.class, tipoContaId);

             if (accountType == null) {
                 throw new IllegalArgumentException("Tipo inexistente");
             }

             BankAccount bankAccount = new BankAccount();
             bankAccount.setAccountType(accountType);
             bankAccount.setClient(bankClient);
             bankAccount.addBankAccount(bankAccount);

             // testa se o cpf ja existe no banco
             if (bankClientDAO.cpfExists(bankClient.getCpf())) {
                 throw new IllegalArgumentException("CPF já existente");
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
             throw new RuntimeException("Erro ao registrar cliente e conta", e);
         } finally {
             if (session != null && session.isOpen()) {
                 session.close();
             }
         }

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
