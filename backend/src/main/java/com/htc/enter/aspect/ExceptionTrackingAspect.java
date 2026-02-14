package com.htc.enter.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.htc.enter.exception.ApplicationException;

@Aspect
@Component
public class ExceptionTrackingAspect {

    private static final Logger log = LoggerFactory.getLogger(ExceptionTrackingAspect.class);

    @AfterThrowing(pointcut = "within(com.htc.enter..*)", throwing = "ex")
    public void trackException(JoinPoint jp, Throwable ex) {
        String traceId = MDC.get("traceId");
        String method = jp.getSignature().toShortString();

        if (ex instanceof ApplicationException) {
            ApplicationException appEx = (ApplicationException) ex;
            log.error("[{}] {}: {} - {}", traceId, method, appEx.getErrorCode(), appEx.getMessage());
        } else {
            log.error("[{}] {}: {}", traceId, method, ex.getMessage());
        }
    }
}
