package com.vn.baseapis.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IpRateLimited {
    int limit();          // số request tối đa
    int durationSeconds(); // thời gian (giây)
}
