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


    public BankClient() {

    }

    public BankClient(String clientName, String phone, String birthDate, String cpf) {
        this.clientName = clientName;
        this.phone = phone;
        this.birthDate = birthDate;
        this.cpf = cpf;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getclientName() {
        return clientName;
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

}
