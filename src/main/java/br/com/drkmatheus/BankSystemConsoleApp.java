package br.com.drkmatheus;

import br.com.drkmatheus.config.HibernateUtil;
import br.com.drkmatheus.dao.*;
import br.com.drkmatheus.entities.BankAccount;
import br.com.drkmatheus.entities.BankAccountType;
import br.com.drkmatheus.entities.BankClient;
import br.com.drkmatheus.entities.BankTransaction;
import br.com.drkmatheus.service.BankClientService;
import br.com.drkmatheus.service.BankTransactionService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class BankSystemConsoleApp {
    private static SessionFactory sessionFactory;
    private static BankClientDAO bankClientDAO;
    private static BankClientService bankClientService;
    private static Scanner scanner;

    public static void main(String[] args) {
        // Inicialização
        initialize();

        // Loop do menu principal
        while (true) {
            exibirMenuPrincipal();
            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir nova linha

            switch (opcao) {
                case 1:
                    registrarNovoCliente();
                    break;
                case 2:
                    realizarLogin();
                    break;
                case 3:
                    System.out.println("Encerrando o sistema...");
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void initialize() {
        // Configurar SessionFactory
        sessionFactory = HibernateUtil.getSessionFactory();

        // Criar DAO e Serviço
        bankClientDAO = new BankClientDAOImpl(sessionFactory);
        bankClientService = new BankClientService(bankClientDAO);

        // Iniciar Scanner
        scanner = new Scanner(System.in);
    }

    private static void exibirMenuPrincipal() {
        System.out.println("\n--- SISTEMA BANCÁRIO ---");
        System.out.println("1. Abrir Conta");
        System.out.println("2. Fazer Login");
        System.out.println("3. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private static void registrarNovoCliente() {
        Session session = HibernateUtil.openSession();
        Transaction transaction = null;
        try {
            BankClient novoCliente = new BankClient();
            BankAccount novaConta = new BankAccount();
            BankAccountType bankAccountType = new BankAccountType();

            System.out.print("Digite o nome completo: ");
            novoCliente.setClientName(scanner.nextLine());

            System.out.print("Digite o CPF (formato 000.000.000-00): ");
            novoCliente.setCpf(scanner.nextLine());
            while (bankClientDAO.cpfExists(novoCliente.getCpf())) {
                System.out.println("CPF já existente");
                System.out.print("Digite o CPF (formato 000.000.000-00): ");
                novoCliente.setCpf(scanner.nextLine());
            }

            System.out.print("Digite o telefone: ");
            novoCliente.setPhone(scanner.nextLine());

            System.out.print("Digite a data de nascimento (dd/mm/yyyy): ");
            String dataNascimento = scanner.nextLine();
            novoCliente.setBirthDate(LocalDate.parse(dataNascimento,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Seleção do tipo de conta
            System.out.println("Escolha o tipo de conta:");
            System.out.println("1 - Conta Corrente");
            System.out.println("2 - Conta Poupança");
            System.out.println("3 - Conta Salário");
            System.out.print("Digite o número do tipo de conta: ");

            int tipoContaEscolhido = scanner.nextInt();
            scanner.nextLine(); // Consumir nova linha

            // Buscar o tipo de conta no banco de dados
            BankAccountType accountType = session.get(BankAccountType.class, tipoContaEscolhido);
            if (accountType == null) {
                throw new IllegalArgumentException("Tipo de conta inexistente");
            }

            // Criação de uma nova conta associada ao tipo de conta
            BankAccount account = new BankAccount();
            account.setAccountType(accountType);

            // Coleta e validação de senha
            while (true) {
                //System.out.print("Digite sua senha (mínimo 8 caracteres, com maiúsculas, minúsculas, números e caractere especial): ");
                System.out.print("Digite sua senha (mínimo 5 caracteres): ");
                String senha = scanner.nextLine();

                if (bankClientService.isPasswordStrong(senha)) {
                    try {
                        bankClientService.registerNewClient(novoCliente, senha, tipoContaEscolhido);
                        System.out.println("Cliente cadastrado com sucesso!");
                        break;
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    System.out.println("Senha não atende aos requisitos de segurança. Tente novamente.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao registrar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void realizarLogin() {
        try {
            System.out.print("Digite seu CPF: ");
            String cpf = scanner.nextLine();

            System.out.print("Digite sua senha: ");
            String senha = scanner.nextLine();

            Optional<BankClient> clienteLogado = bankClientDAO.login(cpf, senha);

            if (clienteLogado.isPresent()) {
                System.out.println("Login realizado com sucesso!");
                exibirMenuClienteLogado(clienteLogado.get());
            } else {
                System.out.println("CPF ou senha inválidos");
            }
        } catch (Exception e) {
            System.out.println("Erro durante o login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static BankAccountType buscarTipoContaPorId(int tipoContaEscolhido) {
        try (Session session = HibernateUtil.openSession()) {
            System.out.println("Buscando tipo de conta com ID: " + tipoContaEscolhido);

            // Tente buscar o BankAccountType pelo ID diretamente
            BankAccountType tipoConta = session.get(BankAccountType.class, tipoContaEscolhido);

            if (tipoConta == null) {
                System.out.println("Tipo de conta não encontrado. ID: " + tipoContaEscolhido);
            } else {
                System.out.println("Tipo de conta encontrado: " + tipoConta.getTypeName());
            }

            return tipoConta;
        } catch (Exception e) {
            System.out.println("Erro ao buscar tipo de conta no banco: " + e.getMessage());
            return null;
        }
    }

    private static void exibirMenuClienteLogado(BankClient cliente) {
        BankAccount account = cliente.getBankAccounts().getFirst();
        while (true) {
            System.out.println("\n--- BEM-VINDO " + cliente.getClientName().toUpperCase() + " ---");
            System.out.println("1. Ver Informações Pessoais");
            System.out.println("2. Abrir Outro Tipo de Conta");
            System.out.println("3. Deposito");
            System.out.println("4. Saque");
            System.out.println("5. Verificar Saldo");
            System.out.println("6. Transferência");
            System.out.println("7. Extrato");
            System.out.println("0. Voltar ao Menu Principal");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir nova linha

            switch (opcao) {
                case 1:
                    exibirInformacoesPessoais(cliente);
                    break;
                case 2:
                    return;
                case 3:
                    realizarDeposito(account);
                    break;
                case 4:
                    realizarSaque(account);
                    break;
                case 5:
                    verificarSaldo(account);
                    break;
                case 6:
                    realizaTransferencia(account);
                    break;
                case 7:
                    imprimirExtrato(account);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void imprimirExtrato(BankAccount account) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);

        try {
            // obter lista de extrato (transacoes)
            List<BankTransaction> extrato = bankTransactionService.getTransactions(account);

            // exibe o extrato
            System.out.println("\n--- Extrato da Conta ---");
            for (BankTransaction bankTransaction : extrato) {
                System.out.printf("Data: %s | Tipo: %s | Valor: $ %.2f%n",
                        bankTransaction.getTransactionDate(),
                        bankTransaction.getTransactionType(),
                        bankTransaction.getTransactionAmount());
            }
        }
        catch (Exception e) {
            System.out.println("Erro ao imprimir extrato: " + e.getMessage());
        }
        finally {
            session.close();
        }

    }

    private static void realizaTransferencia(BankAccount originAccount) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);

        System.out.println("Digite o ID da conta de destino: ");
        int idDestino = Integer.parseInt(scanner.nextLine());

        System.out.println("Digite o valor a ser transferido: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        try {
            // busca a conta de destino pelo ID
            BankAccount targetAccount = bankAccountDAO.findById(idDestino)
                    .orElseThrow(() -> new IllegalArgumentException("Conta de destino nao encontrada"));

            // realiza a transferencia
            bankTransactionService.transfer(originAccount, targetAccount, amount);
            System.out.println("Transferencia realizada com sucesso!");

        }
        catch (Exception e) {
            System.out.println("Erro ao realizar a transferencia: " + e.getMessage());
        }
        finally {
            session.close();
        }
    }

    private static void verificarSaldo(BankAccount account) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);

        try {
            BigDecimal saldo = bankTransactionService.checkBalance(account);
            System.out.println("Saldo atual: $ " + saldo);
        }
        catch (Exception e) {
            System.out.println("Erro ao verificar saldo: " + e.getMessage());
        }
        finally {
            session.close();
        }
    }


    private static void exibirInformacoesPessoais(BankClient cliente) {
        System.out.println("\n--- INFORMAÇÕES PESSOAIS ---");
        System.out.println("Nome: " + cliente.getClientName());
        System.out.println("CPF: " + cliente.getCpf());
        System.out.println("Telefone: " + cliente.getPhone());
        System.out.println("Data de Nascimento: " +
                cliente.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private static void realizarDeposito(BankAccount account) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = HibernateUtil.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session); // instanciando o DAO
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, new BankAccountTypeDAOImpl(sessionFactory));
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);
        System.out.print("Digite o valor a ser depositado: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        try {
            bankTransactionService.deposit(account, amount);
            System.out.println("Depósito realizado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao realizar depósito: " + e.getMessage());
        }
        finally {
            session.close();
        }
    }
    private static void realizarSaque(BankAccount account) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = HibernateUtil.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);

        System.out.print("Digite o valor a ser sacado: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        try {
            bankTransactionService.withdraw(account, amount);
            System.out.println("Saque realizado com sucesso!");
        }
        catch (Exception e) {
            System.out.println("Erro ao realizar saque: " + e.getMessage());
        }
        finally {
            session.close();
        }
    }
}
