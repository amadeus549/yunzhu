package com.hestia.annotation;

import com.hestia.enumeration.OperationType;

import java.lang.annotation.*;

/**
 *自定义注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    OperationType value();
}
