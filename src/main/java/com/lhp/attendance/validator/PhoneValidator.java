package com.lhp.attendance.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<PhoneNumberValidationAnnotationName, String> {

    @Override
    public void initialize(PhoneNumberValidationAnnotationName constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext constraintValidatorContext) {
        return phone != null && phone.matches("[0-9]+")
                && (phone.length() > 8) && (phone.length() <= 10);
    }
}
