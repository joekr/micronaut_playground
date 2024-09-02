package com.dtss.interceptor;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

import javax.inject.Singleton;

import com.dtss.annotation.RateLimited;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class RateLimitingInterceptor implements MethodInterceptor<Object, Object> {

    private final ConcurrentHashMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        AnnotationValue<RateLimited> rateLimitedAnnotation = context.getAnnotation(RateLimited.class);

        if (rateLimitedAnnotation != null) {
            String methodKey = context.getTargetMethod().toString();

            Bucket bucket = bucketCache.computeIfAbsent(methodKey, key -> {
                int capacity = rateLimitedAnnotation.get("capacity", Integer.class).orElse(5);
                int refillTokens = rateLimitedAnnotation.get("refillTokens", Integer.class).orElse(1);
                long refillDurationSeconds = rateLimitedAnnotation.get("refillDurationSeconds", Long.class).orElse(10L);

                Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(refillTokens, Duration.ofSeconds(refillDurationSeconds)));
                return Bucket.builder().addLimit(limit).build();
            });

            if (!bucket.tryConsume(1)) {
                // Return a rate limit exceeded response
                return Mono.just(HttpResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Rate limit exceeded")));
            }
        }
        // Proceed with the original method execution
        return context.proceed();
    }
}