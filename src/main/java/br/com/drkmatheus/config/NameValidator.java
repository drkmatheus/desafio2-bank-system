package br.com.drkmatheus.config;

public class NameValidator {

    public static boolean isValid(String name) {
        // verifica se o campo esta vazio ou so tem espacos
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Verifica se o nome começa ou termina com espaço
        if (name.startsWith(" ") || name.endsWith(" ")) {
            return false;
        }
        // Verifica se o nome contém apenas letras e espaços simples
        if (!name.matches("^[A-Za-z]+( [A-Za-z]+)*$")) {
            return false;
        }

        return true;
    }
}
