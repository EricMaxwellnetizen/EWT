package com.htc.enter.aspect;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggerAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggerAspect.class);
    private static final String TRACE_ID = "traceId";
    
    @Pointcut("within(com.htc.enter.controller..*)")
    public void controllerLayer() {}

    @Pointcut("within(com.htc.enter.serviceimpl..*)")
    public void serviceLayer() {}

    @Around("controllerLayer() || serviceLayer()")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = MDC.get(TRACE_ID);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put(TRACE_ID, traceId);
        }

        String method = joinPoint.getSignature().toShortString();
        log.debug("[{}] -> {}", traceId, method);

        Instant start = Instant.now();
        try {
            Object result = joinPoint.proceed();
            long elapsed = Duration.between(start, Instant.now()).toMillis();
            log.debug("[{}] <- {} ({}ms)", traceId, method, elapsed);
            return result;
        } catch (Throwable ex) {
            long elapsed = Duration.between(start, Instant.now()).toMillis();
            log.error("[{}] !! {} failed after {}ms: {}", traceId, method, elapsed, ex.getMessage());
            throw ex;
        }
    }
}
