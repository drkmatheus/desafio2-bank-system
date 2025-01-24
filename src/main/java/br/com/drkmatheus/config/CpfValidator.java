package br.com.drkmatheus.config;

public class CpfValidator {
    public static boolean isValid(String cpf) {
        // remove caracteres nao numericos por nada ("")
        cpf = cpf.replaceAll("[^0-9]", "");

        // verifica se todos os dígitos são iguais (CPF inválido)
        if (cpf.length() != 11) {
            return false;
        }

        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        return true;
    }
}
