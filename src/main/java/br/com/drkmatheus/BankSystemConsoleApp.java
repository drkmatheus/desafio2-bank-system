package br.com.drkmatheus;

import br.com.drkmatheus.config.HibernateUtil;
import br.com.drkmatheus.dao.BankClientDAO;
import br.com.drkmatheus.dao.BankClientDAOImpl;
import br.com.drkmatheus.entities.BankClient;
import br.com.drkmatheus.service.BankClientService;
import org.hibernate.SessionFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        System.out.println("1. Registrar Novo Cliente");
        System.out.println("2. Fazer Login");
        System.out.println("3. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private static void registrarNovoCliente() {
        try {
            BankClient novoCliente = new BankClient();

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

            // Coleta e validação de senha
            while (true) {
                //System.out.print("Digite sua senha (mínimo 8 caracteres, com maiúsculas, minúsculas, números e caractere especial): ");
                System.out.print("Digite sua senha (mínimo 5 caracteres: ");
                String senha = scanner.nextLine();

                if (bankClientService.isPasswordStrong(senha)) {
                    try {
                        bankClientService.registerNewClient(novoCliente, senha);
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

    private static void exibirMenuClienteLogado(BankClient cliente) {
        while (true) {
            System.out.println("\n--- BEM-VINDO " + cliente.getClientName().toUpperCase() + " ---");
            System.out.println("1. Ver Informações Pessoais");
            System.out.println("2. Voltar ao Menu Principal");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir nova linha

            switch (opcao) {
                case 1:
                    exibirInformacoesPessoais(cliente);
                    break;
                case 2:
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
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
}
