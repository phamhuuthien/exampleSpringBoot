package com.example.learn.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

// định nghĩa documented
@Documented
// ràng buộc là dùng cho ớp phoneValidator
@Constraint(validatedBy = PhoneValidator.class)
//ápđụng cho phương thức và field
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {
    String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
