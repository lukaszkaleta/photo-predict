package com.aceve.hackathon.interceptor;

import com.aceve.hackathon.config.ApiKeyConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyInterceptor.class);

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        String clientIp = getClientIp(request);
        String method = request.getMethod();
        
        logger.info("Request from IP: {} - {} {}", clientIp, method, requestUri);
        
        // Skip API key check for /ping endpoint and public API endpoints
        if (requestUri.equals("/ping") || 
            requestUri.startsWith("/api/photos") || 
            requestUri.startsWith("/api/records")) {
            return true;
        }

        // Check API key for all other API endpoints
        if (requestUri.startsWith("/api/")) {
            String apiKey = request.getHeader(ApiKeyConfig.API_KEY_HEADER);

            if (apiKey == null || !apiKey.equals(ApiKeyConfig.API_KEY)) {
                logger.warn("Unauthorized access attempt from IP: {} - {} {}", clientIp, method, requestUri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or missing API key");
                return false;
            }
        }
        return true;
    }
}
