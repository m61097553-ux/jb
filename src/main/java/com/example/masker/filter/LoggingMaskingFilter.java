package com.example.masker.filter;

import com.example.masker.DTOMaskingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Базовый фильтр для маскировки данных в логах HTTP запросов/ответов
 * Наследуйтесь от этого класса и переопределите doFilterInternal для кастомной логики
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(prefix = "data.masker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingMaskingFilter extends OncePerRequestFilter {
    
    protected final DTOMaskingService dtoMaskingService;
    
    public LoggingMaskingFilter(DTOMaskingService dtoMaskingService) {
        this.dtoMaskingService = dtoMaskingService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Обертываем request и response для возможности чтения тела
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        CachedBodyHttpServletResponse wrappedResponse = new CachedBodyHttpServletResponse(response);
        
        try {
            // Выполняем фильтр
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
            // После выполнения фильтра можно логировать с маскировкой
            logRequest(wrappedRequest);
            logResponse(wrappedResponse);
            
        } finally {
            // Копируем тело ответа обратно в оригинальный response
            wrappedResponse.copyBodyToResponse();
        }
    }
    
    /**
     * Логирует request с маскировкой чувствительных данных
     * Переопределите этот метод для кастомной логики
     */
    protected void logRequest(CachedBodyHttpServletRequest request) {
        try {
            String requestBody = getRequestBody(request);
            if (requestBody != null && !requestBody.isEmpty()) {
                // Пытаемся распарсить как JSON и замаскировать
                String maskedBody = maskRequestBody(requestBody);
                log.info("Request {} {} - Body: {}", 
                    request.getMethod(), 
                    request.getRequestURI(), 
                    maskedBody);
            } else {
                log.info("Request {} {}", request.getMethod(), request.getRequestURI());
            }
        } catch (Exception e) {
            log.warn("Error logging request: {}", e.getMessage());
        }
    }
    
    /**
     * Логирует response с маскировкой чувствительных данных
     * Переопределите этот метод для кастомной логики
     */
    protected void logResponse(CachedBodyHttpServletResponse response) {
        try {
            String responseBody = getResponseBody(response);
            if (responseBody != null && !responseBody.isEmpty()) {
                // Пытаемся распарсить как JSON и замаскировать
                String maskedBody = maskResponseBody(responseBody);
                log.info("Response {} - Body: {}", response.getStatus(), maskedBody);
            } else {
                log.info("Response {}", response.getStatus());
            }
        } catch (Exception e) {
            log.warn("Error logging response: {}", e.getMessage());
        }
    }
    
    /**
     * Получает тело запроса
     */
    protected String getRequestBody(CachedBodyHttpServletRequest request) {
        return request.getBodyAsString();
    }
    
    /**
     * Получает тело ответа
     */
    protected String getResponseBody(CachedBodyHttpServletResponse response) {
        return response.getBodyAsString();
    }
    
    /**
     * Маскирует тело запроса
     * Переопределите для кастомной логики маскировки
     */
    protected String maskRequestBody(String body) {
        return maskJsonBody(body);
    }
    
    /**
     * Маскирует тело ответа
     * Переопределите для кастомной логики маскировки
     */
    protected String maskResponseBody(String body) {
        return maskJsonBody(body);
    }
    
    /**
     * Маскирует JSON тело, пытаясь распарсить его как DTO
     */
    private String maskJsonBody(String jsonBody) {
        try {
            // Пытаемся распарсить JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = dtoMaskingService.getMapper();
            Object jsonObject = mapper.readValue(jsonBody, Object.class);
            
            // Маскируем используя DTOMaskingService
            return dtoMaskingService.maskDTOToString(jsonObject);
        } catch (Exception e) {
            // Если не удалось распарсить как JSON, возвращаем как есть
            return jsonBody;
        }
    }
    
    /**
     * Получает DTOMaskingService для использования в наследниках
     */
    protected DTOMaskingService getDtoMaskingService() {
        return dtoMaskingService;
    }
}

