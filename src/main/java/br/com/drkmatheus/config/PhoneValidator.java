package br.com.drkmatheus.config;

public class PhoneValidator {
    public static boolean isValid(String phone) {
        // Remove todos os caracteres não numéricos
        phone = phone.replaceAll("[^0-9]", "");

        // Verifica se o telefone tem pelo menos 10 dígitos (formato mínimo: DDD + número)
        if (phone.length() < 10 || phone.length() > 11) {
            return false;
        }

        return true;
    }
}
