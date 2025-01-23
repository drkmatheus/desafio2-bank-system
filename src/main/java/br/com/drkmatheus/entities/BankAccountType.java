package br.com.drkmatheus.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

@Entity
@Table(name = "account_type")
public class BankAccountType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "type_name")
    private String typeName;

    public BankAccountType(String typeName) {
            this.typeName = typeName;
    }

    public BankAccountType() {

    }

    public int getId() {
            return id;
    }

    public void setId(int id) {
            this.id = id;
    }

    public String getTypeName() {
            return typeName;
    }

    public void setTypeName(String typeName) {
            this.typeName = typeName;
    }
}

