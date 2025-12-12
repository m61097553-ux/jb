package com.example.masker.example.dto;

import com.example.masker.annotation.Mask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Пример DTO для платежа с различными типами маскировки
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    
    private String paymentId;
    private Double amount;
    
    @Mask(maskChars = "*#", maskLength = 10)
    private String inn;  // Маскируются первые 10 символов символами * и #
    
    @Mask(startIndex = 3, length = 4, maskChar = 'X')
    private String transactionNum;  // Маскируется с 4 по 7 символ (индексы 3, 4, 5, 6) символом X
    
    @Mask
    private String epkId;  // Маскируется полностью
    
    @Mask(nameMaskLength = 2, maskChar = '.')
    private String payerName;  // Первые 2 буквы маскируются точками
    
    @Mask(maskChar = '•')
    private String payerSurname;  // Маскируется полностью символом •
}

