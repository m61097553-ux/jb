package com.example.masker.example.filter;

import com.example.masker.DTOMaskingService;
import com.example.masker.example.dto.UserDTO;
import com.example.masker.filter.CachedBodyHttpServletRequest;
import com.example.masker.filter.LoggingMaskingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Пример кастомного фильтра, наследующегося от LoggingMaskingFilter
 * Демонстрирует как маскировать данные из request перед логированием
 */
@Slf4j
@Component
public class CustomLoggingFilter extends LoggingMaskingFilter {
    
    private final ObjectMapper objectMapper;
    
    public CustomLoggingFilter(DTOMaskingService dtoMaskingService, ObjectMapper objectMapper) {
        super(dtoMaskingService);
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Обертываем request для чтения тела
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        
        // Выполняем базовую логику фильтра
        super.doFilterInternal(wrappedRequest, response, filterChain);
        
        // Дополнительная обработка request с маскировкой
        processRequest(wrappedRequest);
    }
    
    /**
     * Обрабатывает request с маскировкой данных
     */
    private void processRequest(CachedBodyHttpServletRequest request) {
        try {
            String requestBody = getRequestBody(request);
            if (requestBody != null && !requestBody.isEmpty()) {
                // Пытаемся распарсить как UserDTO
                try {
                    UserDTO userDTO = objectMapper.readValue(requestBody, UserDTO.class);
                    
                    // Маскируем DTO перед логированием используя сервис из базового класса
                    String maskedDTO = getDtoMaskingService().maskDTOToString(userDTO);
                    log.info("Processed UserDTO with masking: {}", maskedDTO);
                    
                } catch (Exception e) {
                    // Если не UserDTO, используем общую маскировку из базового класса
                    String maskedBody = maskRequestBody(requestBody);
                    log.info("Processed request body with masking: {}", maskedBody);
                }
            }
        } catch (Exception e) {
            log.warn("Error processing request: {}", e.getMessage());
        }
    }
}

