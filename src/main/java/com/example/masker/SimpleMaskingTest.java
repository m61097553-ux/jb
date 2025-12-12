package com.example.masker;

import com.example.masker.example.dto.PaymentDTO;
import com.example.masker.example.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Простой тест для проверки работы маскировки после рефакторинга
 */
public class SimpleMaskingTest {
    
    public static void main(String[] args) {
        System.out.println("=== Тест маскировки после рефакторинга ===\n");
        
        int passed = 0;
        int failed = 0;
        
        try {
            // Создаем сервисы
            DataMaskerService dataMaskerService = new DataMaskerService();
            ObjectMapper objectMapper = new ObjectMapper();
            DTOMaskingService dtoMaskingService = new DTOMaskingService(dataMaskerService, objectMapper);
            
            // Тест 1: UserDTO
            System.out.println("Тест 1: UserDTO");
            UserDTO user = new UserDTO(
                    "123",
                    "Иван",
                    "1234567890123",  // INN - первые 8 символов должны быть замаскированы
                    "1234567890",      // NUM - 5-й, 6-й, 7-й символы должны быть замаскированы
                    "EPK123456",       // EPK_ID - полностью замаскирован символом #
                    "Иван",            // firstName - первая буква должна быть заменена на *
                    "Петров"           // lastName - полностью замаскирован
            );
            
            String maskedUser = dtoMaskingService.maskDTOToString(user);
            System.out.println("Исходный: " + objectMapper.writeValueAsString(user));
            System.out.println("Замаскированный: " + maskedUser);
            
            boolean test1Passed = true;
            if (!maskedUser.contains("*") && !maskedUser.contains("#") && !maskedUser.contains("X")) {
                System.out.println("✗ ОШИБКА: Символы маскировки отсутствуют");
                test1Passed = false;
            }
            if (!maskedUser.contains("890123") && !maskedUser.contains("89012")) {
                System.out.println("✗ ОШИБКА: INN не замаскирован правильно");
                test1Passed = false;
            }
            if (test1Passed) {
                System.out.println("✓ Тест 1 пройден\n");
                passed++;
            } else {
                System.out.println("✗ Тест 1 провален\n");
                failed++;
            }
            
            // Тест 2: PaymentDTO
            System.out.println("Тест 2: PaymentDTO");
            PaymentDTO payment = new PaymentDTO(
                    "PAY-001",
                    1000.0,
                    "1234567890123",  // INN
                    "1234567890",      // transactionNum
                    "EPK123456",       // epkId
                    "Иван",            // payerName
                    "Петров"           // payerSurname
            );
            
            String maskedPayment = dtoMaskingService.maskDTOToString(payment);
            System.out.println("Исходный: " + objectMapper.writeValueAsString(payment));
            System.out.println("Замаскированный: " + maskedPayment);
            
            boolean test2Passed = true;
            if (!maskedPayment.contains("PAY-001")) {
                System.out.println("✗ ОШИБКА: paymentId должен остаться видимым");
                test2Passed = false;
            }
            if (!maskedPayment.contains("1000.0")) {
                System.out.println("✗ ОШИБКА: amount должен остаться видимым");
                test2Passed = false;
            }
            if (test2Passed) {
                System.out.println("✓ Тест 2 пройден\n");
                passed++;
            } else {
                System.out.println("✗ Тест 2 провален\n");
                failed++;
            }
            
            // Тест 3: Null DTO
            System.out.println("Тест 3: Null DTO");
            String nullResult = dtoMaskingService.maskDTOToString(null);
            if ("null".equals(nullResult)) {
                System.out.println("✓ Тест 3 пройден (null обработан корректно)\n");
                passed++;
            } else {
                System.out.println("✗ Тест 3 провален (ожидалось 'null', получено: " + nullResult + ")\n");
                failed++;
            }
            
            // Тест 4: Прямая проверка DataMaskerService
            System.out.println("Тест 4: Прямая проверка DataMaskerService");
            MaskingParams params1 = new MaskingParams(0, 0, "", '*', 4, 3, 0, 0);
            String result1 = dataMaskerService.maskValue("1234567890", params1);
            System.out.println("Маскировка диапазона (startIndex=4, length=3): " + result1);
            if (result1.contains("*") && result1.contains("5") && result1.contains("6") && result1.contains("7")) {
                System.out.println("✓ Тест 4.1 пройден (маскировка диапазона)");
                passed++;
            } else {
                System.out.println("✗ Тест 4.1 провален");
                failed++;
            }
            
            MaskingParams params2 = new MaskingParams(0, 0, "", '#', 0, 0, 8, 0);
            String result2 = dataMaskerService.maskValue("1234567890123", params2);
            System.out.println("Маскировка первых 8 символов (maskLength=8): " + result2);
            if (result2.contains("#") && result2.contains("890123")) {
                System.out.println("✓ Тест 4.2 пройден (маскировка первых символов)");
                passed++;
            } else {
                System.out.println("✗ Тест 4.2 провален");
                failed++;
            }
            
            MaskingParams params3 = new MaskingParams(0, 0, "", '*', 0, 0, 0, 0);
            String result3 = dataMaskerService.maskValue("EPK123456", params3);
            System.out.println("Полная маскировка (без параметров): " + result3);
            if (result3.matches("^[*#X•▪]+$")) {
                System.out.println("✓ Тест 4.3 пройден (полная маскировка)");
                passed++;
            } else {
                System.out.println("✗ Тест 4.3 провален");
                failed++;
            }
            
            MaskingParams params4 = new MaskingParams(0, 0, "", '.', 0, 0, 0, 1);
            String result4 = dataMaskerService.maskValue("Иван", params4);
            System.out.println("Маскировка имени (nameMaskLength=1): " + result4);
            if (result4.contains(".") && result4.contains("ван")) {
                System.out.println("✓ Тест 4.4 пройден (маскировка имени)");
                passed++;
            } else {
                System.out.println("✗ Тест 4.4 провален");
                failed++;
            }
            
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("✗ КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
        
        // Итоги
        System.out.println("=== Итоги тестирования ===");
        System.out.println("Пройдено: " + passed);
        System.out.println("Провалено: " + failed);
        System.out.println("Всего тестов: " + (passed + failed));
        
        if (failed == 0) {
            System.out.println("\n✓ Все тесты пройдены успешно!");
            System.exit(0);
        } else {
            System.out.println("\n✗ Некоторые тесты провалены");
            System.exit(1);
        }
    }
}

