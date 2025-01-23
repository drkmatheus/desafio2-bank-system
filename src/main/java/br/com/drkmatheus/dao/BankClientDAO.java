package br.com.drkmatheus.dao;

import br.com.drkmatheus.entities.BankClient;

import java.util.Optional;

public interface BankClientDAO {
    Optional<BankClient> findByCpf(String cpf);
    Optional<BankClient> login(String cpf, String rawPassword);
    void save(BankClient bankClient);
    boolean cpfExists(String cpf);

}
