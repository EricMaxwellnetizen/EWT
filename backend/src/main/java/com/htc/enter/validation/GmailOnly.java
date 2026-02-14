package com.htc.enter.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email.List;

@Documented
@Constraint(validatedBy = GmailOnlyValidator.class)
@Target({ ElementType.FIELD })
@Retention(RUNTIME)
public @interface GmailOnly {

	String message() default "Email must be a Gmail address";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };



}
