package com.htc.enter.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CacheMonitoringAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheMonitoringAspect.class);

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object monitorCacheable(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = jp.proceed();
            log.debug("Cache read: {} ({}ms)", jp.getSignature().toShortString(), 
                System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.error("Cache read error: {}", jp.getSignature().toShortString());
            throw t;
        }
    }

    @Around("@annotation(org.springframework.cache.annotation.CachePut)")
    public Object monitorCachePut(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = jp.proceed();
            log.debug("Cache write: {} ({}ms)", jp.getSignature().toShortString(),
                System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.error("Cache write error: {}", jp.getSignature().toShortString());
            throw t;
        }
    }
}
