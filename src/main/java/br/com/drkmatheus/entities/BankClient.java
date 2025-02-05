package br.com.drkmatheus.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_client")
public class BankClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String clientName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "birthdate")
    private LocalDate birthDate;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "password")
    private String password;

    @Column(name = "active")
    private boolean active;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BankAccount> bankAccounts = new ArrayList<>();

    public BankClient() {

    }

    public BankClient(String clientName, String phone, LocalDate birthDate, String cpf, String password) {
        this.clientName = clientName;
        this.phone = phone;
        this.birthDate = birthDate;
        this.cpf = cpf;
        this.password = password;
        this.active = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<BankAccount> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(List<BankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

//    // Método auxiliar para adicionar uma conta
//    public void addBankAccount(BankAccount bankAccount) {
//        bankAccounts.add(bankAccount);
//        bankAccount.setClient(this);
//    }

    // para desativar o usuario
    public void deactivate() {
        this.active = false;
    }
}


