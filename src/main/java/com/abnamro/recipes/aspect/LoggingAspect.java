package com.abnamro.recipes.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Cross-cutting logging aspect for the service layer.
 * Logs method entry with arguments, execution time on success, and exception details on failure.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.abnamro.recipes.service.*.*(..))")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        Object[] args = joinPoint.getArgs();

        log.info("→ {}({})", methodName, Arrays.toString(args));
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            log.info("← {} completed in {}ms", methodName, System.currentTimeMillis() - start);
            return result;
        } catch (Exception ex) {
            log.error("✗ {} failed after {}ms — {}: {}",
                    methodName, System.currentTimeMillis() - start,
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}