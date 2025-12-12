package com.example.masker.example.dto;

import com.example.masker.annotation.Mask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Пример DTO с аннотациями @Mask для маскировки полей
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private String id;
    private String name;
    
    @Mask(maskLength = 8)
    private String inn;  // Маскируются первые 8 символов
    
    @Mask(startIndex = 4, length = 3)
    private String num;  // Маскируется с 5 по 7 символ (индексы 4, 5, 6)
    
    @Mask(maskChar = '#')
    private String epkId;  // Маскируется полностью символом #
    
    @Mask(maskChar = '*', nameMaskLength = 1)
    private String firstName;  // Первая буква маскируется *
    
    @Mask(maskChars = "*#X")
    private String lastName;  // Маскируется полностью с разными символами
}

