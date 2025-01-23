package br.com.drkmatheus.config;

import br.com.drkmatheus.entities.BankAccount;
import br.com.drkmatheus.entities.BankAccountType;
import br.com.drkmatheus.entities.BankClient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(BankClient.class)
                    .addAnnotatedClass(BankAccount.class)
                    .addAnnotatedClass(BankAccountType.class)
                    .buildSessionFactory();
        }
        catch (Exception e) {
            System.err.println("Erro ao criar o SessionFactory" + e);
            throw new ExceptionInInitializerError(e);
        }
    }
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // Método para obter a sessão atual
    public static Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    // Método para abrir uma nova sessão se necessário
    public static Session openSession() {
        Session session = sessionFactory.openSession();
        if (session == null) {
            System.err.println("Erro: Sessão não foi aberta corretamente.");
        }
        return session;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
