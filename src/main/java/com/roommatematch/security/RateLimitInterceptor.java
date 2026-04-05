package com.roommatematch.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, Deque<Long>> requestCounts = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 60;
    private static final long TIME_WINDOW_MS = 60000;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String ip = request.getRemoteAddr();

        Deque<Long> timestamps = requestCounts.computeIfAbsent(ip, k -> new ArrayDeque<>());

        long currentTime = System.currentTimeMillis();

        synchronized (timestamps) {
            // Remove timestamps older than TIME_WINDOW_MS
            while (!timestamps.isEmpty() && timestamps.peekFirst() < currentTime - TIME_WINDOW_MS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= MAX_REQUESTS) {
                log.warn("Rate limit exceeded for IP: {}", ip);
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"status\":429,\"message\":\"Too many requests. Please wait before trying again.\",\"timestamp\":\"" +
                                LocalDateTime.now().toString() + "\"}");
                return false;
            }

            timestamps.addLast(currentTime);
        }

        return true;
    }
}
