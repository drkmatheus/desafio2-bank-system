package br.com.drkmatheus.service;

import br.com.drkmatheus.dao.BankClientDAO;
import br.com.drkmatheus.dao.BankClientDAOImpl;
import br.com.drkmatheus.entities.BankClient;
import org.hibernate.SessionFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BankClientService {
    private final BankClientDAO bankClientDAO;
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

    public boolean registerNewClient(BankClient bankClient, String rawPassword) {
        // testa se o cpf ja existe no banco
        if (bankClientDAO.cpfExists(bankClient.getCpf())) {
            throw new IllegalArgumentException("CPF já existente");
        }

        // criptografa senha
        //bankClient.setPassword(passwordEncoder.encode(rawPassword));
        bankClient.setPassword(rawPassword);

        // salva o cliente
        bankClientDAO.save(bankClient);
        return true;
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
