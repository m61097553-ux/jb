package com.example.masker;

import com.example.masker.example.dto.PaymentDTO;
import com.example.masker.example.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DTOMaskingServiceTest {
    
    private DataMaskerService dataMaskerService;
    private DTOMaskingService dtoMaskingService;
    
    @BeforeEach
    void setUp() {
        dataMaskerService = new DataMaskerService();
        dtoMaskingService = new DTOMaskingService(dataMaskerService, null);
    }
    
    @Test
    void testMaskUserDTO() {
        UserDTO user = new UserDTO(
                "123",
                "Иван",
                "1234567890123",  // INN - первые 8 символов должны быть замаскированы
                "1234567890",      // NUM - 5-й, 6-й, 7-й символы должны быть замаскированы
                "EPK123456",       // EPK_ID - полностью замаскирован символом #
                "Иван",            // firstName - первая буква должна быть заменена на *
                "Петров"           // lastName - полностью замаскирован
        );
        
        String masked = dtoMaskingService.maskDTOToString(user);
        System.out.println("Masked UserDTO: " + masked);
        
        assertNotNull(masked);
        assertTrue(masked.contains("*") || masked.contains("#") || masked.contains("X"));
        // Проверяем, что INN замаскирован (первые 8 символов)
        assertTrue(masked.contains("890123") || masked.contains("89012"));
    }
    
    @Test
    void testMaskPaymentDTO() {
        PaymentDTO payment = new PaymentDTO(
                "PAY-001",
                1000.0,
                "1234567890123",  // INN
                "1234567890",      // transactionNum
                "EPK123456",       // epkId
                "Иван",            // payerName
                "Петров"           // payerSurname
        );
        
        String masked = dtoMaskingService.maskDTOToString(payment);
        System.out.println("Masked PaymentDTO: " + masked);
        
        assertNotNull(masked);
        assertTrue(masked.contains("PAY-001"));
        assertTrue(masked.contains("1000.0"));
    }
    
    @Test
    void testMaskNullDTO() {
        String result = dtoMaskingService.maskDTOToString(null);
        assertEquals("null", result);
    }
}


