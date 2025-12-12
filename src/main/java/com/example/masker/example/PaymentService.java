package com.example.masker.example;

import com.example.masker.DTOMaskingService;
import com.example.masker.example.dto.PaymentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Пример сервиса для обработки платежей с маскировкой
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final DTOMaskingService dtoMaskingService;
    
    /**
     * Пример обработки платежа с логированием замаскированного DTO
     */
    public void processPayment(PaymentDTO paymentDTO) {
        String maskedDTO = dtoMaskingService.maskDTOToString(paymentDTO);
        log.info("Обработка платежа: {}", maskedDTO);
    }
    
    /**
     * Пример создания платежа
     */
    public PaymentDTO createPayment(String paymentId, Double amount) {
        PaymentDTO payment = new PaymentDTO(
                paymentId,
                amount,
                "1234567890123",
                "1234567890",
                "EPK123456",
                "Иван",
                "Петров"
        );
        
        String masked = dtoMaskingService.maskDTOToString(payment);
        log.info("Создан платеж: {}", masked);
        
        return payment;
    }
}

