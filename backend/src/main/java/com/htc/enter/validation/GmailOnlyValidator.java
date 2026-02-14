package com.htc.enter.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GmailOnlyValidator implements ConstraintValidator<GmailOnly, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return value.toLowerCase().endsWith("@gmail.com");
    }
}
