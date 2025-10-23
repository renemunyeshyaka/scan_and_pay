package com.scan_and_pay.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> resetTimes = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip rate limiting for certain endpoints
        if (shouldSkipRateLimiting(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIpAddress(httpRequest);
        long currentTime = System.currentTimeMillis();
        String key = clientIp + ":" + (currentTime / 60000); // Minute-based key

        // Get or initialize count
        int count = requestCounts.getOrDefault(key, 0);
        
        // Check if limit exceeded
        if (count >= MAX_REQUESTS_PER_MINUTE) {
            sendRateLimitResponse(httpResponse, "Too many requests. Please try again in a minute.");
            return;
        }

        // Update count
        requestCounts.put(key, count + 1);
        resetTimes.put(key, currentTime + 60000); // Set reset time to 1 minute from now

        chain.doFilter(request, response);
    }

    private boolean shouldSkipRateLimiting(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/public/") || 
               path.contains("/management/") ||
               path.contains("/swagger") ||
               path.contains("/api-docs") ||
               path.contains("/h2-console") ||
               path.startsWith("/api/auth/");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            String.format("{\"error\": \"Rate limit exceeded\", \"message\": \"%s\"}", message)
        );
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Start cleanup thread
        startCleanupThread();
    }

    @Override
    public void destroy() {
        requestCounts.clear();
        resetTimes.clear();
    }

    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(30000); // Cleanup every 30 seconds
                    cleanupExpiredEntries();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        requestCounts.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            Long resetTime = resetTimes.get(key);
            return resetTime == null || currentTime > resetTime;
        });
    }
}