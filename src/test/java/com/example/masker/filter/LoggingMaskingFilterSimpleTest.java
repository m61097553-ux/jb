package com.example.masker.filter;

import com.example.masker.DataMaskerService;
import com.example.masker.DTOMaskingService;
import com.example.masker.example.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Простой тест для проверки работы LoggingMaskingFilter
 */
class LoggingMaskingFilterSimpleTest {
    
    private LoggingMaskingFilter filter;
    private DTOMaskingService dtoMaskingService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        DataMaskerService dataMaskerService = new DataMaskerService();
        dtoMaskingService = new DTOMaskingService(dataMaskerService, objectMapper);
        filter = new LoggingMaskingFilter(dtoMaskingService);
    }
    
    @Test
    void testMaskRequestBody() throws Exception {
        // Подготовка UserDTO с чувствительными данными
        UserDTO userDTO = new UserDTO(
                "123",
                "Иван",
                "1234567890123",  // INN - первые 8 символов должны быть замаскированы
                "1234567890",      // NUM - 5-й, 6-й, 7-й символы должны быть замаскированы
                "EPK123456",       // EPK_ID - полностью замаскирован символом #
                "Иван",            // firstName - первая буква должна быть заменена на *
                "Петров"           // lastName - полностью замаскирован
        );
        
        String requestBody = objectMapper.writeValueAsString(userDTO);
        
        // Тестируем маскировку через защищенный метод (используем рефлексию)
        java.lang.reflect.Method maskMethod = LoggingMaskingFilter.class
                .getDeclaredMethod("maskRequestBody", String.class);
        maskMethod.setAccessible(true);
        
        String maskedBody = (String) maskMethod.invoke(filter, requestBody);
        
        // Проверки
        assertNotNull(maskedBody);
        assertNotEquals(requestBody, maskedBody, "Тело должно быть замаскировано");
        
        // Проверяем, что INN замаскирован (первые 8 символов)
        assertTrue(maskedBody.contains("890123") || maskedBody.contains("89012"), 
                "INN должен содержать незамаскированные последние символы");
        
        // Проверяем наличие символов маскировки
        assertTrue(maskedBody.contains("*") || maskedBody.contains("#") || maskedBody.contains("X"),
                "Должны присутствовать символы маскировки");
        
        System.out.println("Original: " + requestBody);
        System.out.println("Masked:   " + maskedBody);
    }
    
    @Test
    void testMaskResponseBody() throws Exception {
        // Подготовка UserDTO
        UserDTO userDTO = new UserDTO(
                "123",
                "Иван",
                "1234567890123",
                "1234567890",
                "EPK123456",
                "Иван",
                "Петров"
        );
        
        String responseBody = objectMapper.writeValueAsString(userDTO);
        
        // Тестируем маскировку через защищенный метод
        java.lang.reflect.Method maskMethod = LoggingMaskingFilter.class
                .getDeclaredMethod("maskResponseBody", String.class);
        maskMethod.setAccessible(true);
        
        String maskedBody = (String) maskMethod.invoke(filter, responseBody);
        
        // Проверки
        assertNotNull(maskedBody);
        assertNotEquals(responseBody, maskedBody, "Тело ответа должно быть замаскировано");
        
        System.out.println("Original Response: " + responseBody);
        System.out.println("Masked Response:   " + maskedBody);
    }
    
    @Test
    void testMaskInvalidJson() throws Exception {
        // Некорректный JSON
        String invalidJson = "{invalid json}";
        
        // Тестируем маскировку
        java.lang.reflect.Method maskMethod = LoggingMaskingFilter.class
                .getDeclaredMethod("maskRequestBody", String.class);
        maskMethod.setAccessible(true);
        
        String result = (String) maskMethod.invoke(filter, invalidJson);
        
        // Некорректный JSON должен вернуться как есть
        assertEquals(invalidJson, result, "Некорректный JSON должен вернуться без изменений");
    }
    
    @Test
    void testMaskEmptyBody() throws Exception {
        // Пустое тело
        String emptyBody = "";
        
        java.lang.reflect.Method maskMethod = LoggingMaskingFilter.class
                .getDeclaredMethod("maskRequestBody", String.class);
        maskMethod.setAccessible(true);
        
        String result = (String) maskMethod.invoke(filter, emptyBody);
        
        // Пустое тело должно вернуться как есть
        assertEquals(emptyBody, result);
    }
    
    @Test
    void testGetDtoMaskingService() {
        // Проверяем доступ к сервису
        DTOMaskingService service = filter.getDtoMaskingService();
        assertNotNull(service);
        assertEquals(dtoMaskingService, service);
    }
}


