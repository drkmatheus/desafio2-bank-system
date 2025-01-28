package br.com.drkmatheus.dao;

import br.com.drkmatheus.config.HibernateUtil;
import br.com.drkmatheus.entities.BankAccount;
import br.com.drkmatheus.entities.BankClient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Scanner;


public class BankClientDAOImpl implements BankClientDAO {

    // pra estabelecer contrato de sessao (ler e escrever dados no BD)
    private final SessionFactory sessionFactory;
    // adiciona instancia do bcrypt
    //private final BCryptPasswordEncoder passwordEncoder;

    public BankClientDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        //this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public Optional<BankClient> findByCpf(String cpf) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT c FROM BankClient c LEFT JOIN FETCH c.bankAccounts WHERE c.cpf = :cpf";
            Query<BankClient> query = session.createQuery(hql, BankClient.class);
            query.setParameter("cpf", cpf);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


 //    @Override
//    public Optional<BankClient> findByCpf(String cpf) {
//        try (Session session = sessionFactory.openSession()) {
//            Query<BankClient> query = session.createQuery(
//                    "FROM BankClient WHERE cpf =:cpf", BankClient.class
//            );
//            query.setParameter("cpf", cpf);
//            return query.uniqueResultOptional();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return Optional.empty();
//        }
//    }

    @Override
    public Optional<BankClient> login(String cpf, String rawPassword) {
        try (Session session = sessionFactory.openSession()) {
            // Busca o cliente pelo CPF, já carregando as contas bancárias
            Optional<BankClient> optionalClient = findByCpf(cpf);

            if (optionalClient.isEmpty()) {
                System.out.println("Client's CPF not found: " + cpf);
                return Optional.empty();
            }

            BankClient bankClient = optionalClient.get();

            // Verifica a senha
            if (rawPassword.equals(bankClient.getPassword())) {
                if (!bankClient.isActive()) {
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("\n" +
                            "This account is deactivated. Do you want to reactivate it?");
                    System.out.println("1 - Yes");
                    System.out.println("2 - No");
                    System.out.print("Enter your option: ");

                    int opcao = scanner.nextInt();

                    if (opcao == 1) {
                        this.reactivateAccount(bankClient);
                        System.out.println("Account reactivated successfully!");
                        return Optional.of(bankClient);
                    }
                    else {
                        System.out.println("Login cancelled.");
                        return Optional.empty();
                    }
                }
                return Optional.of(bankClient);
            } else {
                System.out.println("Wrong password. Try again.");
                return Optional.empty();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void reactivateAccount(BankClient bankClient) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                if (bankClient.isActive()) {
                    System.out.println("The account is already active.");
                    return;
                }

                bankClient.setActive(true);
                // Também reativa a conta bancária associada
                for (BankAccount account : bankClient.getBankAccounts()) {
                    account.setActive(true);
                    session.update(account);
                }
                session.update(bankClient);

                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }


//    @Override
//    public Optional<BankClient> login(String cpf, String rawPassword) {
//        Session session = HibernateUtil.openSession();
//
//        try {
//            Query<BankClient> query = session.createQuery(
//                    "FROM BankClient WHERE cpf = :cpf", BankClient.class);
//            query.setParameter("cpf", cpf);
//
//            BankClient bankClient = query.uniqueResult();
//
//            if (bankClient == null) {
//                System.out.println("Cliente nao encontrado com o cpf: " + cpf);
//                return Optional.empty();
//            }
//
//            if (rawPassword.equals(bankClient.getPassword())) {
//                return Optional.of(bankClient);
//            }
//            else {
//                System.out.println("Senha incorreta. Tente novamente.");
//                return Optional.empty();
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return Optional.empty();
//        }
//        finally {
//            session.close();
//        }
//    }

    @Override
    public void save(BankClient bankClient) {
        try (Session session = sessionFactory.openSession()) {
            // verificando se ja existe cliente com mesmo cpf
            Optional<BankClient> check = findByCpf(bankClient.getCpf());
            if (check.isPresent()) {
                throw new IllegalArgumentException("There is already a client registered with this CPF.");
            }
            session.beginTransaction();

            // antes de salvar, codificar a senha
//            if (bankClient.getPassword() != null) {
//                bankClient.setPassword(passwordEncoder.encode(bankClient.getPassword())
//                );
//            }

            session.save(bankClient);
            session.getTransaction().commit();
        }
        catch (Exception e) {
            throw new RuntimeException("\n" +
                    "Error in saving client.", e);
        }
    }

    public void update(BankClient cliente) {
        Session session = HibernateUtil.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.update(cliente); // Atualiza o cliente no banco de dados
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("\"Error in saving client: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    // verificador de existencia de cpf no banco de dados
    @Override
    public boolean cpfExists(String cpf) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(*) FROM BankClient WHERE cpf = :cpf", Long.class
            )
                    .setParameter("cpf", cpf)
                    .uniqueResult();
            return count > 0;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
