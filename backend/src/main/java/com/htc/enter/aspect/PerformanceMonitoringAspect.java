package com.htc.enter.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceMonitoringAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);
    private static final long SLOW_THRESHOLD = 1000;

    @Pointcut("within(com.htc.enter.serviceimpl..*)")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object monitor(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = jp.proceed();
            long duration = System.currentTimeMillis() - start;
            
            if (duration > SLOW_THRESHOLD) {
                log.warn("Slow method: {}.{} took {}ms",
                    jp.getTarget().getClass().getSimpleName(),
                    jp.getSignature().getName(),
                    duration);
            }
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - start;
            log.error("Failed after {}ms: {}", duration, jp.getSignature().toShortString());
            throw t;
        }
    }
}
