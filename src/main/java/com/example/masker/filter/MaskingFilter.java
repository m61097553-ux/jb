package com.example.masker.filter;

import com.example.masker.config.MaskingProperties;
import com.example.masker.service.JsonMaskingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Фильтр для маскировки данных в теле HTTP запросов и ответов
 */
public class MaskingFilter extends OncePerRequestFilter {
    
    private final MaskingProperties properties;
    private final JsonMaskingService jsonMaskingService;
    
    public MaskingFilter(MaskingProperties properties, JsonMaskingService jsonMaskingService) {
        this.properties = properties;
        this.jsonMaskingService = jsonMaskingService;
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        // Обработка запроса
        HttpServletRequest processedRequest = processRequest(request);
        
        // Обработка ответа
        MaskedBodyHttpServletResponse maskedResponse = processResponse(response);
        
        // Продолжаем цепочку фильтров
        if (maskedResponse != null) {
            filterChain.doFilter(processedRequest, maskedResponse);
            // После обработки маскируем ответ, если нужно
            processResponseAfterFilter(maskedResponse, response);
        } else {
            filterChain.doFilter(processedRequest, response);
        }
    }
    
    /**
     * Обрабатывает запрос: маскирует тело, если нужно
     */
    private HttpServletRequest processRequest(HttpServletRequest request) throws IOException {
        if (!properties.isRequestEnabled()) {
            return request;
        }
        
        if (!isJsonContent(request.getContentType())) {
            return request;
        }
        
        // Читаем оригинальное тело запроса
        byte[] originalBodyBytes = StreamUtils.copyToByteArray(request.getInputStream());
        
        if (originalBodyBytes.length == 0) {
            return request;
        }
        
        // Преобразуем в строку и маскируем
        String originalBody = new String(originalBodyBytes, StandardCharsets.UTF_8);
        String maskedBody = jsonMaskingService.maskJson(originalBody);
        
        // Создаем обертку с замаскированным телом
        return new MaskedBodyHttpServletRequest(request, maskedBody);
    }
    
    /**
     * Обрабатывает ответ: создает обертку для кэширования тела
     */
    private MaskedBodyHttpServletResponse processResponse(HttpServletResponse response) {
        if (!properties.isResponseEnabled()) {
            return null;
        }
        return new MaskedBodyHttpServletResponse(response);
    }
    
    /**
     * Обрабатывает ответ после выполнения фильтров: маскирует и записывает обратно
     */
    private void processResponseAfterFilter(MaskedBodyHttpServletResponse maskedResponse, 
                                           HttpServletResponse originalResponse) throws IOException {
        if (!properties.isResponseEnabled()) {
            return;
        }
        
        String responseBody = maskedResponse.getCachedBodyAsString();
        if (responseBody == null || responseBody.isEmpty()) {
            return;
        }
        
        // Проверяем, является ли контент JSON
        String contentType = maskedResponse.getContentType();
        if (!isJsonContent(contentType)) {
            // Если не JSON, просто копируем как есть
            byte[] bodyBytes = maskedResponse.getCachedBodyAsBytes();
            if (bodyBytes.length > 0 && !originalResponse.isCommitted()) {
                originalResponse.setContentLength(bodyBytes.length);
                originalResponse.getOutputStream().write(bodyBytes);
                originalResponse.getOutputStream().flush();
            }
            return;
        }
        
        // Маскируем JSON ответ
        String maskedResponseBody = jsonMaskingService.maskJson(responseBody);
        
        // Записываем замаскированное тело обратно в оригинальный response
        if (!originalResponse.isCommitted()) {
            byte[] maskedBytes = maskedResponseBody.getBytes(StandardCharsets.UTF_8);
            originalResponse.setContentLength(maskedBytes.length);
            originalResponse.getOutputStream().write(maskedBytes);
            originalResponse.getOutputStream().flush();
        }
    }
    
    /**
     * Проверяет, является ли контент JSON
     * Оптимизировано: быстрая проверка без лишних операций
     */
    private boolean isJsonContent(String contentType) {
        if (contentType == null) {
            return false;
        }
        // startsWith уже покрывает contains, поэтому достаточно одной проверки
        return contentType.startsWith("application/json");
    }
    
    /**
     * Получить замаскированное тело запроса
     * Может использоваться для логирования
     */
    public String getMaskedRequestBody(HttpServletRequest request) {
        if (request instanceof MaskedBodyHttpServletRequest) {
            MaskedBodyHttpServletRequest maskedRequest = (MaskedBodyHttpServletRequest) request;
            try {
                byte[] bodyBytes = StreamUtils.copyToByteArray(maskedRequest.getInputStream());
                return new String(bodyBytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Получить замаскированное тело ответа
     * Может использоваться для логирования
     */
    public String getMaskedResponseBody(HttpServletResponse response) {
        if (response instanceof MaskedBodyHttpServletResponse) {
            MaskedBodyHttpServletResponse maskedResponse = (MaskedBodyHttpServletResponse) response;
            return maskedResponse.getCachedBodyAsString();
        }
        return null;
    }
}
