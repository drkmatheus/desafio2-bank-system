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
INSERT INTO account_type (type_name) VALUES ('Checking Account');
INSERT INTO account_type (type_name) VALUES ('Savings Account');
INSERT INTO account_type (type_name) VALUES ('Salary Account');

-- Tabela para os clientes bancários
CREATE TABLE bank_client (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    phone VARCHAR(15),
    birthdate DATE,
    active BOOLEAN DEFAULT TRUE NOT NULL
);

-- Tabela para as contas bancárias
CREATE TABLE bank_account (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    account_type_id INT NOT NULL,
    account_types VARCHAR(255) DEFAULT '',
    balance DECIMAL(10, 2) DEFAULT 0.00,
    active BOOLEAN DEFAULT TRUE NOT NULL,
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
