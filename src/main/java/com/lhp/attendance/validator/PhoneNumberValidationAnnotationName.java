package com.lhp.attendance.validator;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface PhoneNumberValidationAnnotationName {
    String message() default "Message returned";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
