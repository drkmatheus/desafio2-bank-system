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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class App {
    private static BankClientDAO bankClientDAO;
    private static BankClientService bankClientService;
    private static BankAccountTypeDAO bankAccountTypeDAO;
    private static Scanner scanner;

    public static void main(String[] args) {
        initialize();

        while (true) {
            showMainMenu();
            String option = scanner.next();
            scanner.nextLine();

            try {
                int optionChoosed = Integer.parseInt(option);
                switch (optionChoosed) {
                    case 1:
                        performLogin();
                        break;
                    case 2:
                        registerNewClient();
                        break;
                    case 0:
                        System.out.println("Shuting down...");
                        return;
                    default:
                        System.out.println("Invalid option! Please try again.");
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private static void initialize() {
        // Configurar SessionFactory
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        // Criar DAO e Serviço
        bankClientDAO = new BankClientDAOImpl(sessionFactory);
        bankClientService = new BankClientService(bankClientDAO);

        bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);

        // Iniciar Scanner
        scanner = new Scanner(System.in);
    }

    // print main menu
    private static void showMainMenu() {
        System.out.println("\n========= Main Menu =========");
        System.out.println("|| 1. Login                ||");
        System.out.println("|| 2. Account Opening      ||");
        System.out.println("|| 0. Exit                 ||");
        System.out.println("=============================");
        System.out.print("Choose an option: ");
    }

    private static void registerNewClient() {
        Session session = HibernateUtil.openSession();

        try {
            BankClient newClient = new BankClient();

            // Validating name
            String name;
            while (true) {
                System.out.print("Enter full name: ");
                name = scanner.nextLine();

                if (NameValidator.isValid(name)) {
                    newClient.setClientName(name);
                    break;
                } else {
                    System.out.println("Invalid name. Use only letters and a space between words, with no leading or trailing spaces.");
                }
            }

            // Validating CPF (Brazilian Social Security Number)
            String cpf;
            while (true) {
                System.out.print("Enter CPF (numbers only): ");
                cpf = scanner.nextLine();

                if (!CpfValidator.isValid(cpf)) {
                    System.out.println("Invalid CPF. CPF must contain exactly 11 numeric digits.");
                    continue;
                }

                if (bankClientDAO.cpfExists(cpf)) {
                    System.out.println("CPF already registered. Please use another.");
                } else {
                    newClient.setCpf(cpf);
                    break;
                }
            }

            // Validating phone number
            String phone;
            while (true) {
                System.out.print("Enter phone number (numbers only, including area code): ");
                phone = scanner.nextLine();
                if (PhoneValidator.isValid(phone)) {
                    newClient.setPhone(phone);
                    break;
                } else {
                    System.out.println("Invalid phone number. Enter only numbers, including area code (10 or 11 digits).");
                }
            }

            // Validating birthdate
            String birthDate;
            while (true) {
                System.out.print("Enter birth date (dd/mm/yyyy): ");
                birthDate = scanner.nextLine();

                if (DateValidator.isValid(birthDate, "dd/MM/yyyy")) {
                    newClient.setBirthDate(LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    break;
                } else {
                    System.out.println("Invalid birth date. Use the format dd/mm/yyyy.");
                }
            }

            // Account type selection
            System.out.println("Choose the account type:");
            System.out.println("1 - Checking Account");
            System.out.println("2 - Savings Account");
            System.out.println("3 - Salary Account");
            int selectedAccountType = validateAccountType();

            // Fetch the account type from the database
            BankAccountType accountType = session.get(BankAccountType.class, selectedAccountType);
            if (accountType == null) {
                throw new IllegalArgumentException("Account type does not exist");
            }

            // Create a new account associated with the account type
            BankAccount account = new BankAccount();
            account.setAccountType(accountType);

            // Password collection and validation
            while (true) {
                // System.out.print("Enter your password (minimum 8 characters, with uppercase, lowercase, numbers, and special character): ");
                System.out.print("Enter your password (minimum 5 characters): ");
                String password = scanner.nextLine();

                if (bankClientService.isPasswordStrong(password)) {
                    try {
                        bankClientService.registerNewClient(newClient, password, selectedAccountType);
                        System.out.println("Client successfully registered!");
                        break;
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    System.out.println("Password does not meet the security requirements. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
    }

    private static void performLogin() {
        try {
            System.out.print("Enter your CPF or type 0 to cancel login: ");
            String cpf = scanner.nextLine();
            checkCancellation(cpf);

            // Validating CPF
            if (!CpfValidator.isValid(cpf)) {
                System.out.println("Invalid CPF. CPF must contain exactly 11 numeric digits.");
                return;
            }

            System.out.print("Enter your password or type 0 to cancel login: ");
            String password = scanner.nextLine();

            Optional<BankClient> loggedInClient = bankClientDAO.login(cpf, password);

            if (loggedInClient.isPresent()) {
                System.out.println("Login successful!");
                bankMenu(loggedInClient.get());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // System.out.println("Error during login: " + e.getMessage());
            // e.printStackTrace();
        }
    }

    // prints all bank menu
    public static void bankMenu(BankClient bankClient) {

        BankAccount account = bankClient.getBankAccounts().getFirst();

        while (true) {
            System.out.println("\n--- WELCOME " + bankClient.getClientName().toUpperCase() + " ---");
            System.out.println("Account ID: " + account.getIdAccount());
            System.out.println("Account Status: " + (account.isActive() ? "Active" : "Inactive"));
            System.out.println("You have accounts of the following types: \n");

            // Gets the client's existing account
            BankAccount existingAccount = bankClient.getBankAccounts().get(0);

            // Lists the accounts linked to the client
            Set<Integer> accountTypeIds = existingAccount.getAccountTypeIds();

            if (accountTypeIds.isEmpty()) {
                System.out.println("No linked accounts");
            } else {
                for (int accountTypeId : accountTypeIds) {
                    BankAccountType accountType = bankAccountTypeDAO.findById(accountTypeId)
                            .orElseThrow(() -> new IllegalArgumentException("Account type does not exist."));

                    System.out.println("- " + accountType.getTypeName());
                }
            }

            System.out.println("\n=========== Bank Menu ===========");
            System.out.println("|| 1. Deposit                  ||");
            System.out.println("|| 2. Withdraw                 ||");
            System.out.println("|| 3. Check Balance            ||");
            System.out.println("|| 4. Transfer                 ||");
            System.out.println("|| 5. Add Account Type         ||");
            System.out.println("|| 6. Remove Account Type      ||");
            System.out.println("|| 7. Deactivate Account       ||");
            System.out.println("|| 8. Bank Statement           ||");
            System.out.println("|| 9. Personal Info            ||");
            System.out.println("|| 0. Exit                     ||");
            System.out.println("=================================");
            System.out.print("Choose an option: ");

            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    performDeposit(account);
                    break;
                case 2:
                    performWithdraw(account);
                    break;
                case 3:
                    checkBalance(account);
                    break;
                case 4:
                    performTransfer(account);
                    break;
                case 5:
                    addAccountTypeToExistingAccount(bankClient);
                    break;
                case 6:
                    removeAccountTypeFromExistingAccount(bankClient);
                    break;
                case 7:
                    deactivateAccount(account);
                    break;
                case 8:
                    printStatement(account);
                    break;
                case 9:
                    displayPersonalInformation(bankClient);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }

    // method to deactivate an account
    private static void deactivateAccount(BankAccount account) {
        System.out.println("\n=== Account Deactivation ===");

        if (!account.isActive()) {
            System.out.println("You do not have permission to deactivate your account. Your account is already deactivated.");
            return;
        }

        System.out.println("Are you sure you want to deactivate your account? This action cannot be undone.");
        System.out.println("1 - Yes");
        System.out.println("2 - No");
        System.out.print(("Enter your option: "));
        int option = scanner.nextInt();

        if (option == 1) {
            try {

                SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
                Session session = sessionFactory.openSession();
                //BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
                BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
                BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
                bankAccountDAO.deactivateAccount(account.getIdAccount());
                System.out.println("Account deactivated successfully.");
                System.out.println("Thank you for using our services.");

                // Ends the program after deactivation
                System.exit(0);
            } catch (Exception e) {
                System.out.println("Error deactivating account: " + e.getMessage());
            }
        } else {
            System.out.println("Operation cancelled.");
        }
    }

    // method to remove a bank account type from an existing account
    private static void removeAccountTypeFromExistingAccount(BankClient client) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankClientService bankClientService = new BankClientService(
                new BankClientDAOImpl(sessionFactory),
                new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO),
                bankAccountTypeDAO
        );

        try {
            System.out.println("Choose the account type to remove:");
            System.out.println("1 - Checking Account");
            System.out.println("2 - Savings Account");
            System.out.println("3 - Salary Account");
            System.out.println("0 - Cancel");
            System.out.print(("Enter the account type number: "));

            int selectedAccountType = scanner.nextInt();

            switch (selectedAccountType) {
                case 0:
                    return;
            }

            bankClientService.removeAccountType(client, selectedAccountType);
            System.out.println("Account type removed successfully!");
        } catch (Exception e) {
            System.out.println("Error removing account type: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    // method to add a bank account type from an existing account
    private static void addAccountTypeToExistingAccount(BankClient client) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankClientService bankClientService = new BankClientService(
                new BankClientDAOImpl(sessionFactory),
                new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO),
                bankAccountTypeDAO
        );

        try {
            System.out.println("Choose the account type to add:");
            System.out.println("1 - Checking Account");
            System.out.println("2 - Savings Account");
            System.out.println("3 - Salary Account");
            System.out.println("0 - Cancel");
            System.out.print(("Enter the account type number: "));

            int selectedAccountType = scanner.nextInt();

            switch (selectedAccountType) {
                case 0:
                    return;
            }

            bankClientService.addAccountType(client, selectedAccountType);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // System.out.println("Error adding account type: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    // method to print a statement of the current account in session
    private static void printStatement(BankAccount account) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);

        if (!account.isActive()) {
            System.out.println("You do not have permission to view statements. Your account is deactivated.");
            return;
        }

        try {
            // Get list of statements (transactions)
            List<BankTransaction> statement = bankTransactionService.getTransactions(account);

            // Display the statement
            System.out.println("\n--- Account Statement ---");
            for (BankTransaction bankTransaction : statement) {
                System.out.printf("Date: %s | Type: %s | Amount: $ %.2f%n",
                        bankTransaction.getTransactionDate(),
                        bankTransaction.getTransactionType(),
                        bankTransaction.getTransactionAmount());
            }
        } catch (Exception e) {
            System.out.println("\nError printing statement: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    // method to perform a transference operation
    private static void performTransfer(BankAccount originAccount) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);

        if (!originAccount.isActive()) {
            System.out.println("You do not have permission to make transfers. Your account is deactivated.");
            return;
        }

        try {
            System.out.print(("Enter the target account ID: "));
            int targetAccountId = scanner.nextInt();

            if (targetAccountId <= 0) {
                System.out.println("Error: Negative ID.");
                return;
            }

            // Check if the target account ID is the same as the origin account ID
            if (targetAccountId == originAccount.getIdAccount()) {
                System.out.println("Error: You cannot transfer to your own account.");
                return;
            }
            // Search for the target account by ID
            Optional<BankAccount> optionalTargetAccount = bankAccountDAO.findById(targetAccountId);
            if (!optionalTargetAccount.isPresent()) {
                System.out.println("Error: Target account not found.");
                return;
            }

            BigDecimal amount = readBigDecimal("Enter the amount to be transferred");

            // Check if the amount is valid
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Error: The transfer amount must be greater than zero.");
                return;
            }

            BankAccount targetAccount = optionalTargetAccount.get();

            // Check if the target account is active
            if (!targetAccount.isActive()) {
                System.out.println("Error: The target account is deactivated.");
                return;
            }
            // Perform the transfer
            bankTransactionService.transfer(originAccount, targetAccount, amount);
            System.out.println("Transfer successful!");
        } catch (Exception e) {
            System.out.println("Error performing the transfer: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    // method to print balance of the account
    private static void checkBalance(BankAccount account) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);

        if (!account.isActive()) {
            System.out.println("You do not have permission to check balance. Your account is deactivated.");
            return;
        }

        try {
            BigDecimal balance = bankTransactionService.checkBalance(account);
            System.out.println("Current balance: $" + balance);
        } catch (Exception e) {
            System.out.println("Error checking balance: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    // method to display personal infos on db
    private static void displayPersonalInformation(BankClient client) {
        System.out.println("\n--- PERSONAL INFORMATION ---");
        System.out.println("Name: " + client.getClientName());
        System.out.println("CPF: " + client.getCpf());
        System.out.println("Phone: " + client.getPhone());
        System.out.println("Date of Birth: " +
                client.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    // method to perform a deposit operation
    private static void performDeposit(BankAccount account) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = HibernateUtil.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session); // Instantiating the DAO
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, new BankAccountTypeDAOImpl(sessionFactory));
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);

        if (!account.isActive()) {
            System.out.println("You do not have permission to make a deposit. Your account is deactivated.");
            return;
        }

        BigDecimal amount = readBigDecimal("Enter the amount to be deposited");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Invalid deposit amount. The amount must be greater than zero.");
            return;
        }

        try {
            bankTransactionService.deposit(account, amount);
            System.out.println("Deposit successful!");
        } catch (Exception e) {
            System.out.println("Error performing deposit: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    // method to perform a withdraw operation
    private static void performWithdraw(BankAccount account) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = HibernateUtil.openSession();
        BankTransactionDAO bankTransactionDAO = new BankTransactionDAOImpl(session);
        BankAccountTypeDAO bankAccountTypeDAO = new BankAccountTypeDAOImpl(sessionFactory);
        BankAccountDAO bankAccountDAO = new BankAccountDAOImpl(sessionFactory, bankAccountTypeDAO);
        BankTransactionService bankTransactionService = new BankTransactionService(bankTransactionDAO, bankAccountDAO);

        if (!account.isActive()) {
            System.out.println("You do not have permission to make withdraws. Your account is deactivated.");
            return;
        }

        BigDecimal amount = readBigDecimal("Enter the amount to be withdraw");

        try {
            bankTransactionService.withdraw(account, amount);
            System.out.println("Withdraw successful!");
        } catch (Exception e) {
            System.out.println("Error performing withdraw: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    // integer input helper
    private static int readInteger(String message) {
        while (true) {
            System.out.print(message + " (or type 0 to cancel): ");
            String input = scanner.nextLine();

            try {
                int value = Integer.parseInt(input);
                if (value == 0) {
                    throw new OperationCancelledException("Operation cancelled by the user.");
                }

                return Integer.parseInt(input);
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // bigdecimal input helper
    private static BigDecimal readBigDecimal(String message) {
        while (true) {
            System.out.print(message + " (or type 0 to cancel): ");
            scanner.nextLine();
            String input = scanner.nextLine();

            try {
                BigDecimal value = new BigDecimal(input);
                if (value.compareTo(BigDecimal.ZERO) == 0) {
                    throw new OperationCancelledException("Operation cancelled by the user.");
                }
                return value;
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // canceller input helper
    private static void checkCancellation(String input) {
        if (input.equalsIgnoreCase("0") || input.equalsIgnoreCase("cancel")) {
            throw new OperationCancelledException("Operation cancelled by the user.");
        }
    }

    // account type input helper
    private static int validateAccountType() {
        while (true) {
            //scanner.next();
            int accountType = readInteger("Enter the account type");

            // Check if the account type is between 1 and 3
            if (accountType >= 1 && accountType <= 3) {
                return accountType;
            }
            else {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }
}