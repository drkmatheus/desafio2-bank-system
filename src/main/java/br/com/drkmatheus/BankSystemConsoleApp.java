package br.com.drkmatheus;

import br.com.drkmatheus.config.*;
import br.com.drkmatheus.dao.*;
import br.com.drkmatheus.entities.BankAccount;
import br.com.drkmatheus.entities.BankAccountType;
import br.com.drkmatheus.entities.BankClient;
import br.com.drkmatheus.entities.BankTransaction;
import br.com.drkmatheus.exception.OperationCancelledException;
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
import java.util.Set;

public class BankSystemConsoleApp {
    private static SessionFactory sessionFactory;
    private static BankClientDAO bankClientDAO;
    private static BankClientService bankClientService;
    private static BankAccountTypeDAO bankAccountTypeDAO;
    private static Scanner scanner;


    public static void main(String[] args) {
        // Inicialização
        initialize();

        // Loop do menu principal
        while (true) {
            exibirMenuPrincipal();
            String opcao = scanner.nextLine();

            // conversor de entrada pra nao quebrar o menu

            try {
                int opcaoEscolhida = Integer.parseInt(opcao);
                switch (opcaoEscolhida) {
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
            catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número.");
            }
        }
    }

    private static void initialize() {
        // Configurar SessionFactory
        sessionFactory = HibernateUtil.getSessionFactory();

        // Criar DAO e Serviço
        bankClientDAO = new BankClientDAOImpl(sessionFactory);
        bankClientService = new BankClientService(bankClientDAO);

        bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);

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

            // validando nome
            String name;
            while (true) {
                System.out.print("Digite o nome completo: ");
                name = scanner.nextLine();

                if (NameValidator.isValid(name)) {
                    novoCliente.setClientName(name);
                    break;
                }
                else {
                    System.out.println("Nome inválido. Use apenas letras e um espaço entre palavras, sem espaços no início ou no fim.");
                }

            }

            // validando cpf
            String cpf;
            while (true) {
                System.out.print("Digite o CPF (apenas numeros): ");
                cpf = scanner.nextLine();

                if (!CpfValidator.isValid(cpf)) {
                    System.out.println("CPF inválido. O CPF deve conter exatamente 11 dígitos numéricos.");
                    continue;
                }

                if (bankClientDAO.cpfExists(cpf)) {
                    System.out.println("CPF já cadastrado. Use outro.");
                }
                else {
                    novoCliente.setCpf(cpf);
                    break;
               }

            }


            // validacao telefone
            String phone;
            while (true) {
                System.out.print("Digite o telefone (apenas numeros e com DDD): ");
                phone = scanner.nextLine();
                if (PhoneValidator.isValid(phone)) {
                novoCliente.setPhone(phone);
                break;
                }
                else {
                    System.out.println("Telefone inválido. Digite apenas números, com DDD (10 ou 11 dígitos).");
                }

            }

            // validacao data de nascimento
            String birthDate;
            while (true) {
                System.out.print("Digite a data de nascimento (dd/mm/aaaa): ");
                birthDate = scanner.nextLine();

                if (DateValidator.isValid(birthDate, "dd/MM/yyyy")) {
                    novoCliente.setBirthDate(LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    break;
                }
                else {
                    System.out.println("Data de nascimento inválida. Digite no formato dd/mm/aaaa.");
                }

            }


            // Seleção do tipo de conta
            System.out.println("Escolha o tipo de conta:");
            System.out.println("1 - Conta Corrente");
            System.out.println("2 - Conta Poupança");
            System.out.println("3 - Conta Salário");
            int tipoContaEscolhido = validarTipoConta();

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
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
    }

    private static void realizarLogin() {
        try {
            System.out.print("Digite seu CPF ou digite 0 para cancelar o login: ");
            String cpf = scanner.nextLine();
            verificarCancelamento(cpf);

            // validando cpf
            if (!CpfValidator.isValid(cpf)) {
                System.out.println("CPF inválido. O CPF deve conter exatamente 11 dígitos numéricos.");
                return;
            }

            System.out.print("Digite sua senha ou digite 0 para cancelar o login: ");
            String senha = scanner.nextLine();

            Optional<BankClient> clienteLogado = bankClientDAO.login(cpf, senha);

            if (clienteLogado.isPresent()) {
                System.out.println("Login realizado com sucesso!");
                exibirMenuClienteLogado(clienteLogado.get());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //System.out.println("Erro durante o login: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    private static void exibirMenuClienteLogado(BankClient cliente) {
        BankAccount account = cliente.getBankAccounts().getFirst();
        while (true) {
            System.out.println("\n--- BEM-VINDO " + cliente.getClientName().toUpperCase() + " ---");
            System.out.println("ID da conta: " + account.getIdAccount());
            System.out.println("Status da conta: " + (account.isActive() ? "Ativo" : "Inativo"));
            System.out.println("Você possui contas do tipo: \n");
            // obtem a conta existente do cliente
            BankAccount contaExistente = cliente.getBankAccounts().get(0);

            // lista contas vinculadas ao cliente
            Set<Integer> accountTypeIds = contaExistente.getAccountTypeIds();

            if (accountTypeIds.isEmpty()) {
                System.out.println("Nenhuma conta vinculada");
            }
            else {
                for (int tipoContaId : accountTypeIds) {
                    BankAccountType accountType = bankAccountTypeDAO.findById(tipoContaId)
                            .orElseThrow(() -> new IllegalArgumentException("Tipo de conta inexistente."));

                    System.out.println("- " + accountType.getTypeName());
                }
            }

            // exibe o menu principal
            System.out.println("\n==== MENU =====");
            System.out.println("1. Ver Informações Pessoais");
            System.out.println("2. Abrir Outro Tipo de Conta");
            System.out.println("3. Deposito");
            System.out.println("4. Saque");
            System.out.println("5. Verificar Saldo");
            System.out.println("6. Transferência");
            System.out.println("7. Extrato");
            System.out.println("8. Desativar conta");
            System.out.println("9. Remover tipo de conta");
            System.out.println("0. Voltar ao Menu Principal");

            int opcao = lerInteiro("Escolha uma opção");

            switch (opcao) {
                case 1:
                    exibirInformacoesPessoais(cliente);
                    break;
                case 2:
                    addAccountTypeToExistingAccount(cliente);
                    break;
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
                case 8:
                    desativaConta(account);
                    break;
                case 9:
                    removeAccountTypeFromExistingAccount(cliente);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void desativaConta(BankAccount account) {
        System.out.println("\n=== Desativação de Conta ===");

        if (!account.isActive()) {
            System.out.println("Voce nao tem permissao para desativar sua conta. Sua conta já está desativada.");
            return;
        }

        System.out.println("Tem certeza que deseja desativar sua conta? Esta ação não poderá ser desfeita.");
        System.out.println("1 - Sim");
        System.out.println("2 - Não");
        int opcao = lerInteiro("Digite sua opção");

        if (opcao == 1) {
            try {
                SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
                Session session = sessionFactory.openSession();
                BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
                BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
                BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
                bankAccountDAO.deactivateAccount(account.getIdAccount());
                System.out.println("Conta desativada com sucesso.");
                System.out.println("Obrigado por utilizar nossos serviços.");
                // Encerra o programa após desativação
                System.exit(0);
            } catch (Exception e) {
                System.out.println("Erro ao desativar conta: " + e.getMessage());
            }
        } else {
            System.out.println("Operação cancelada.");
        }
    }


    private static void removeAccountTypeFromExistingAccount(BankClient cliente) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankClientService bankClientService = new BankClientService(
                new BankClientDAOImpl(sessionFactory),
                new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO),
                bankAccountTypeDAO
        );

        try {
            System.out.println("Escolha o tipo de conta para remover:");
            System.out.println("1 - Conta Corrente");
            System.out.println("2 - Conta Poupança");
            System.out.println("3 - Conta Salário");
            System.out.println("0 - Cancelar");
            int tipoContaEscolhido = lerInteiro("Digite o número do tipo de conta");
            switch (tipoContaEscolhido) {
                case 0:
                    return;
            }

            bankClientService.removeAccountType(cliente, tipoContaEscolhido);
            System.out.println("Tipo de conta removido com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao remover tipo de conta: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private static void addAccountTypeToExistingAccount(BankClient cliente) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankClientService bankClientService = new BankClientService(
                new BankClientDAOImpl(sessionFactory),
                new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO),
                bankAccountTypeDAO
        );

        try {
            System.out.println("Escolha o tipo de conta para adicionar:");
            System.out.println("1 - Conta Corrente");
            System.out.println("2 - Conta Poupança");
            System.out.println("3 - Conta Salário");
            System.out.println("0 - Cancelar");
            int tipoContaEscolhido = lerInteiro("Digite o número do tipo de conta");
            switch (tipoContaEscolhido) {
                case 0:
                    return;
            }

            bankClientService.addAccountType(cliente, tipoContaEscolhido);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            //System.out.println("Erro ao adicionar tipo de conta: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private static void imprimirExtrato(BankAccount account) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);

        if (!account.isActive()) {
            System.out.println("Voce nao tem permissao para visualizar extratos. Sua conta está desativada.");
            return;
        }

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

        if (!originAccount.isActive()) {
            System.out.println("Voce nao tem permissao para realizar transferencias. Sua conta está desativada.");
            return;
        }

        try {
            int idDestino = lerInteiro("Digite o ID da conta de destino");

            if (idDestino <= 0) {
                System.out.println("Erro: ID negativo.");
                return;
            }

            // verifica ID da conta de destino é o mesmo da conta de origem
            if (idDestino == originAccount.getIdAccount()) {
                System.out.println("Erro: Voce nao pode transferir para a propria conta.");
                return;
            }
            // Busca a conta de destino pelo ID
            Optional<BankAccount> optionalTargetAccount = bankAccountDAO.findById(idDestino);
            if (!optionalTargetAccount.isPresent()) {
                System.out.println("Erro: Conta de destino não encontrada.");
                return;
            }

            BigDecimal amount = lerBigDecimal("Digite o valor a ser transferido");

            // verifica se o valor é válido
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Erro: O valor de transferência deve ser maior que zero.");
                return;
            }

            BankAccount targetAccount = optionalTargetAccount.get();

            // verifica se a conta de destino esta ativa
            if (!targetAccount.isActive()) {
                System.out.println("Erro: A conta de destino está desativada.");
                return;
            }
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

        if (!account.isActive()) {
            System.out.println("Voce nao tem permissao para verificar saldo. Sua conta está desativada.");
            return;
        }

        try {
            BigDecimal saldo = bankTransactionService.checkBalance(account);
            System.out.println("Saldo atual: $" + saldo);
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

        if (!account.isActive()) {
            System.out.println("Voce nao tem permissao para realizar um deposito. Sua conta está desativada.");
            return;
        }

        BigDecimal amount = lerBigDecimal("Digite o valor a ser depositado");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Valor inválido para depósito. O valor deve ser maior que zero.");
            return;
        }

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

        if (!account.isActive()) {
            System.out.println("Voce nao tem permissao para realizar saques. Sua conta está desativada.");
            return;
        }

        BigDecimal amount = lerBigDecimal("Digite o valor a ser sacado");

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

    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem + " (ou digite 0 para cancelar): ");
            String entrada = scanner.nextLine().trim();

            try {
                int valor = Integer.parseInt(entrada);
                if (valor == 0) {
                    throw new OperationCancelledException("Operação cancelada pelo usuário.");
                }

                return Integer.parseInt(entrada);
            }
            catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um numero.");
            }
        }
    }

    private static BigDecimal lerBigDecimal(String mensagem) {
        while (true) {
            System.out.print(mensagem + " (ou digite 0 para cancelar): ");
            String entrada = scanner.nextLine();

            try {
                BigDecimal valor = new BigDecimal(entrada);
                if (valor.compareTo(BigDecimal.ZERO) == 0) {
                    throw new OperationCancelledException("Operação cancelada pelo usuário.");
                }
                return valor;
            }
            catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um numero.");
            }
        }
    }

    private static void verificarCancelamento(String entrada) {
        if (entrada.equalsIgnoreCase("0") || entrada.equalsIgnoreCase("cancelar")) {
            throw new OperationCancelledException("Operação cancelada pelo usuário.");
        }
    }

    private static int validarTipoConta() {
        while (true) {
            int tipoConta = lerInteiro("Digite o tipo de conta");

            // verifica se o tipo da conta está entre 1 e 3
            if (tipoConta >= 1 && tipoConta <= 3) {
                return tipoConta;
            }
            else {
                System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }
}
