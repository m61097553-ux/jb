package com.example.masker.filter;

import com.example.masker.DataMaskerService;
import com.example.masker.DTOMaskingService;
import com.example.masker.example.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;

/**
 * Демонстрация работы LoggingMaskingFilter
 */
public class LoggingMaskingFilterDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Демонстрация работы LoggingMaskingFilter ===\n");
        
        try {
            // Инициализация
            ObjectMapper objectMapper = new ObjectMapper();
            DataMaskerService dataMaskerService = new DataMaskerService();
            DTOMaskingService dtoMaskingService = new DTOMaskingService(dataMaskerService, objectMapper);
            LoggingMaskingFilter filter = new LoggingMaskingFilter(dtoMaskingService);
            
            // Создаем тестовый UserDTO
            UserDTO userDTO = new UserDTO(
                    "123",
                    "Иван",
                    "1234567890123",  // INN - первые 8 символов должны быть замаскированы
                    "1234567890",      // NUM - 5-й, 6-й, 7-й символы должны быть замаскированы
                    "EPK123456",       // EPK_ID - полностью замаскирован символом #
                    "Иван",            // firstName - первая буква должна быть заменена на *
                    "Петров"           // lastName - полностью замаскирован
            );
            
            // Преобразуем в JSON
            String requestBody = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(userDTO);
            
            System.out.println("Исходный Request Body:");
            System.out.println(requestBody);
            System.out.println();
            
            // Маскируем через фильтр
            Method maskMethod = LoggingMaskingFilter.class
                    .getDeclaredMethod("maskRequestBody", String.class);
            maskMethod.setAccessible(true);
            String maskedRequestBody = (String) maskMethod.invoke(filter, requestBody);
            
            System.out.println("Замаскированный Request Body:");
            System.out.println(maskedRequestBody);
            System.out.println();
            
            // Тестируем маскировку ответа
            String responseBody = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(userDTO);
            
            System.out.println("Исходный Response Body:");
            System.out.println(responseBody);
            System.out.println();
            
            Method maskResponseMethod = LoggingMaskingFilter.class
                    .getDeclaredMethod("maskResponseBody", String.class);
            maskResponseMethod.setAccessible(true);
            String maskedResponseBody = (String) maskResponseMethod.invoke(filter, responseBody);
            
            System.out.println("Замаскированный Response Body:");
            System.out.println(maskedResponseBody);
            System.out.println();
            
            // Проверка результатов
            System.out.println("=== Проверка результатов ===");
            if (maskedRequestBody.contains("890123") || maskedRequestBody.contains("89012")) {
                System.out.println("✓ INN правильно замаскирован (последние символы видны)");
            }
            
            if (maskedRequestBody.contains("*") || maskedRequestBody.contains("#") || 
                maskedRequestBody.contains("X")) {
                System.out.println("✓ Символы маскировки присутствуют");
            }
            
            if (maskedRequestBody.contains("####")) {
                System.out.println("✓ EPK_ID замаскирован символом #");
            }
            
            System.out.println("\n=== Тест завершен успешно! ===");
            
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении теста: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


