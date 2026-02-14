package com.htc.enter.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    // Rate limits per role
    private static final int ADMIN_RATE_LIMIT = 200; // requests per minute
    private static final int MANAGER_RATE_LIMIT = 100;
    private static final int EMPLOYEE_RATE_LIMIT = 50;
    private static final int ANONYMOUS_RATE_LIMIT = 20;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip rate limiting for health check endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.equals("/health")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String key = getClientKey(request);
        Bucket bucket = resolveBucket(key);
        
        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for key: {}", key);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Please try again later.\"}");
        }
    }
    
    private Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createBucket(key));
    }
    
    private Bucket createBucket(String key) {
        int limit = getRateLimitForKey(key);
        Bandwidth bandwidth = Bandwidth.classic(
            limit,
            Refill.intervally(limit, Duration.ofMinutes(1))
        );
        return Bucket.builder()
            .addLimit(bandwidth)
            .build();
    }
    
    private int getRateLimitForKey(String key) {
        // Extract role from key (format: "username:role" or "ip:ANONYMOUS")
        if (key.contains(":")) {
            String role = key.split(":")[1];
            return switch (role) {
                case "ADMIN" -> ADMIN_RATE_LIMIT;
                case "SENIOR_MANAGER", "MANAGER" -> MANAGER_RATE_LIMIT;
                case "EMPLOYEE" -> EMPLOYEE_RATE_LIMIT;
                default -> ANONYMOUS_RATE_LIMIT;
            };
        }
        return ANONYMOUS_RATE_LIMIT;
    }
    
    private String getClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
            
            return username + ":" + role;
        }
        
        // For unauthenticated requests, use IP address
        String clientIp = getClientIP(request);
        return clientIp + ":ANONYMOUS";
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
