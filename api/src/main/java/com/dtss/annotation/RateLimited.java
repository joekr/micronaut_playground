package com.dtss.annotation;

import io.micronaut.aop.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dtss.interceptor.RateLimitingInterceptor;

import io.micronaut.context.annotation.Type;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Type(RateLimitingInterceptor.class)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RateLimited {
    int capacity() default 100;
    int refillTokens() default 10;
    long refillDurationSeconds() default 10;
}

