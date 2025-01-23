-- Dropar as tabelas caso já existam
DROP TABLE IF EXISTS bank_transaction;
DROP TABLE IF EXISTS bank_account;
DROP TABLE IF EXISTS bank_client;
DROP TABLE IF EXISTS account_type;
DROP database if exists bank_system;

-- Criando o banco de dados
CREATE DATABASE bank_system;

-- Usando o banco de dados
USE bank_system;

-- Tabela para os tipos de conta
CREATE TABLE account_type (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE
);

-- Inserir tipos de conta
INSERT INTO account_type (type_name) VALUES ('Conta Corrente');
INSERT INTO account_type (type_name) VALUES ('Conta Poupança');
INSERT INTO account_type (type_name) VALUES ('Conta Salário');

-- Tabela para os clientes bancários
CREATE TABLE bank_client (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL
    cpf VARCHAR(14) NOT NULL UNIQUE,
    phone VARCHAR(15),
    birthdate DATE
);

-- Tabela para as contas bancárias
CREATE TABLE bank_account (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    account_type_id INT NOT NULL,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (client_id) REFERENCES bank_client(id) ON DELETE CASCADE,
    FOREIGN KEY (account_type_id) REFERENCES account_type(id) ON DELETE CASCADE
);

-- Tabela para as transações bancárias
CREATE TABLE bank_transaction (
    id SERIAL PRIMARY KEY,
    account_id INT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES bank_account(id) ON DELETE CASCADE
);

-- Exemplo de inserção de clientes
INSERT INTO bank_client (name, cpf, phone, birthdate, password)
VALUES ('João Silva', '123.456.789-00', '11987654321', '1990-05-15', '$2a$10$GxQ5x4RjG4z3zMz3z4RjG4z3zMz3z4RjG4z3zMz3z4RjG4z3zMz3');

INSERT INTO bank_client (name, cpf, phone, birthdate, password)
VALUES ('Maria Oliveira', '987.654.321-00', '11876543210', '1985-09-22', '$2a$10$GxQ5x4RjG4z3zMz3z4RjG4z3zMz3z4RjG4z3zMz3z4RjG4z3zMz3');

-- Exemplo de criação de contas para os clientes
-- Criando uma conta para João Silva (Conta Corrente)
INSERT INTO bank_account (client_id, account_type_id, balance) 
VALUES ((SELECT id FROM bank_client WHERE cpf = '123.456.789-00'), 
        (SELECT id FROM account_type WHERE type_name = 'Conta Corrente'), 
        1000.00);

-- Criando uma conta para Maria Oliveira (Conta Poupança)
INSERT INTO bank_account (client_id, account_type_id, balance) 
VALUES ((SELECT id FROM bank_client WHERE cpf = '987.654.321-00'), 
        (SELECT id FROM account_type WHERE type_name = 'Conta Poupança'), 
        1500.00);

-- Exemplo de inserção de transações
-- Transação de saque para João Silva
INSERT INTO bank_transaction (account_id, transaction_type, amount)
VALUES ((SELECT id FROM bank_account WHERE client_id = (SELECT id FROM bank_client WHERE cpf = '123.456.789-00')),
        'SAQUE', 200.00);

-- Transação de depósito para Maria Oliveira
INSERT INTO bank_transaction (account_id, transaction_type, amount)
VALUES ((SELECT id FROM bank_account WHERE client_id = (SELECT id FROM bank_client WHERE cpf = '987.654.321-00')),
        'DEPÓSITO', 500.00);