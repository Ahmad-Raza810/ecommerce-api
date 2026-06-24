package com.example.ecommerce.filter;

import com.example.ecommerce.common.ApiError;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.config.RateLimitProperties;
import com.example.ecommerce.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties, JwtService jwtService, ObjectMapper objectMapper) {
        this.properties = properties;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    private Bucket createBucket(int capacity, int refill, int durationMinutes) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refill, Duration.ofMinutes(durationMinutes))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR",
                "X-Real-IP"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                int commaIndex = ip.indexOf(',');
                if (commaIndex != -1) {
                    return ip.substring(0, commaIndex).trim();
                }
                return ip.trim();
            }
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();

        // 1. Check for excluded paths
        for (String pattern : properties.getExcludedPaths()) {
            if (pathMatcher.match(pattern, uri)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 2. Identify the rate limit configuration to apply
        RateLimitProperties.PathLimit pathLimit = null;
        String matchedPattern = null;

        for (Map.Entry<String, RateLimitProperties.PathLimit> entry : properties.getPathLimits().entrySet()) {
            if (pathMatcher.match(entry.getKey(), uri)) {
                pathLimit = entry.getValue();
                matchedPattern = entry.getKey();
                break;
            }
        }

        // 3. Resolve client key (JWT username if present, otherwise IP address)
        String clientKey;
        boolean isAuthenticated = false;

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtService.extractUsername(token);
                if (username != null) {
                    clientKey = "user:" + username;
                    isAuthenticated = true;
                } else {
                    clientKey = "ip:" + resolveClientIp(request);
                }
            } catch (Exception e) {
                clientKey = "ip:" + resolveClientIp(request);
            }
        } else {
            clientKey = "ip:" + resolveClientIp(request);
        }

        // 4. Resolve capacity and bucket config
        int capacity;
        int refill;
        int durationMinutes;
        String bucketKey;

        if (pathLimit != null) {
            capacity = pathLimit.getCapacity();
            refill = pathLimit.getRefill();
            durationMinutes = pathLimit.getDurationMinutes();
            bucketKey = clientKey + ":" + matchedPattern;
        } else {
            if (isAuthenticated) {
                capacity = properties.getDefaultAuthenticatedCapacity();
                refill = properties.getDefaultAuthenticatedRefill();
                durationMinutes = properties.getDefaultAuthenticatedDurationMinutes();
            } else {
                capacity = properties.getDefaultAnonymousCapacity();
                refill = properties.getDefaultAnonymousRefill();
                durationMinutes = properties.getDefaultAnonymousDurationMinutes();
            }
            bucketKey = clientKey + ":default";
        }

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> createBucket(capacity, refill, durationMinutes));

        // 5. Consume token and set headers
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Limit", String.valueOf(capacity));
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.setHeader("X-Rate-Limit-Reset", String.valueOf(Math.max(0L, probe.getNanosToWaitForRefill() / 1_000_000_000L)));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("X-Rate-Limit-Limit", String.valueOf(capacity));
            response.setHeader("X-Rate-Limit-Remaining", "0");
            response.setHeader("X-Rate-Limit-Reset", String.valueOf(Math.max(0L, probe.getNanosToWaitForRefill() / 1_000_000_000L)));

            ApiResponse<Void> apiResponse = ApiResponse.failure(
                    "Too Many Requests",
                    List.of(new ApiError(null, "Rate limit exceeded. Please try again later."))
            );
            objectMapper.writeValue(response.getWriter(), apiResponse);
        }
    }
}
