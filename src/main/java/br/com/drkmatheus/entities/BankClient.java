package br.com.drkmatheus.entities;

import jakarta.persistence.*;

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
    private String birthDate;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "password")
    private String password;


    public BankClient() {

    }

    public BankClient(String clientName, String phone, String birthDate, String cpf, String password) {
        this.clientName = clientName;
        this.phone = phone;
        this.birthDate = birthDate;
        this.cpf = cpf;
        this.password = password;
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

    public void setFirstName(String clientName) {
        this.clientName = clientName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
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
}
