package com.petshop.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class CpfValidator implements ConstraintValidator<Cpf, String> {

    // Digits only, or the usual mask 000.000.000-00 (each separator optional).
    private static final Pattern FORMAT = Pattern.compile("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return isValid(value);
    }

    public static boolean isValid(String value) {
        if (!FORMAT.matcher(value).matches()) {
            return false;
        }
        String digits = value.replaceAll("\\D", "");

        // 000.000.000-00, 111.111.111-11 … pass the checksum but are not real CPFs.
        if (digits.chars().distinct().count() == 1) {
            return false;
        }
        return checkDigit(digits, 9) == digits.charAt(9) - '0'
                && checkDigit(digits, 10) == digits.charAt(10) - '0';
    }

    /** Check digit over the first {@code length} digits (weights length+1 … 2, mod 11). */
    private static int checkDigit(String digits, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (digits.charAt(i) - '0') * (length + 1 - i);
        }
        int remainder = (sum * 10) % 11;
        return remainder == 10 ? 0 : remainder;
    }
}
