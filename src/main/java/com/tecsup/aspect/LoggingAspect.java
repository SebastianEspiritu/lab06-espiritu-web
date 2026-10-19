package com.tecsup.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.tecsup.service.*.*(..))")
    public void antes(JoinPoint joinPoint) {
        System.out.println("▶ Ejecutando: "
                + joinPoint.getSignature().getName());
    }

    @AfterReturning("execution(* com.tecsup.service.*.*(..))")
    public void despues(JoinPoint joinPoint) {
        System.out.println("✔ Finalizó: "
                + joinPoint.getSignature().getName());
    }
}