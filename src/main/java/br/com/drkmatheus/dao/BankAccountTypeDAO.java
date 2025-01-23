package br.com.drkmatheus.dao;

import br.com.drkmatheus.entities.BankAccountType;

import java.util.List;
import java.util.Optional;

public interface BankAccountTypeDAO {
    Optional<BankAccountType> findByTypeName(String typeName);
    void save(BankAccountType bankAccountType);
    public List<BankAccountType> findAll();
}
