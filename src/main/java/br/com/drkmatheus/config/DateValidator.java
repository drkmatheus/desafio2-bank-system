package br.com.drkmatheus.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateValidator {

    public static boolean isValid(String dataNascimento, String formato) {
        try {
            LocalDate.parse(dataNascimento, DateTimeFormatter.ofPattern(formato));
            return true;
        }
        catch (DateTimeParseException e) {
            return false;
        }
    }
}
