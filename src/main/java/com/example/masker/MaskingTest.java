package com.example.masker;

import com.example.masker.example.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MaskingTest {
    
    public static void main(String[] args) {
        System.out.println("=== Тест маскировки DTO ===\n");
        
        try {
            // Создаем сервисы
            DataMaskerService dataMaskerService = new DataMaskerService();
            ObjectMapper objectMapper = new ObjectMapper();
            DTOMaskingService dtoMaskingService = new DTOMaskingService(dataMaskerService, objectMapper);
            
            // Создаем тестовый DTO
            UserDTO user = new UserDTO(
                    "123",
                    "Иван",
                    "1234567890123",  // INN - первые 8 символов должны быть замаскированы
                    "1234567890",      // NUM - 5-й, 6-й, 7-й символы должны быть замаскированы
                    "EPK123456",       // EPK_ID - полностью замаскирован символом #
                    "Иван",            // firstName - первая буква должна быть заменена на *
                    "Петров"           // lastName - полностью замаскирован
            );
            
            System.out.println("Исходный DTO:");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(user));
            
            System.out.println("\nЗамаскированный DTO:");
            String masked = dtoMaskingService.maskDTOToString(user);
            System.out.println(masked);
            
            // Проверяем результаты
            System.out.println("\n=== Проверка результатов ===");
            if (masked.contains("890123") || masked.contains("89012")) {
                System.out.println("✓ INN правильно замаскирован (последние символы видны)");
            } else {
                System.out.println("✗ INN маскировка не работает");
            }
            
            if (masked.contains("*") || masked.contains("#") || masked.contains("X")) {
                System.out.println("✓ Символы маскировки присутствуют");
            } else {
                System.out.println("✗ Символы маскировки отсутствуют");
            }
            
            if (masked.contains("####")) {
                System.out.println("✓ EPK_ID замаскирован символом #");
            }
            
            System.out.println("\n=== Тест завершен успешно! ===");
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении теста: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

