package com.example.masker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для автоматической маскировки полей DTO при логировании
 * Логика маскировки определяется параметрами аннотации, а не типом
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mask {
    
    /**
     * Количество видимых символов с начала
     */
    int visibleStart() default 2;
    
    /**
     * Количество видимых символов с конца
     */
    int visibleEnd() default 2;
    
    /**
     * Символы для маскировки (разные символы для разных позиций)
     * Если не указано, используется символ по умолчанию из конфигурации
     * Можно указать один символ или несколько (будут циклически применяться)
     * Примеры: "*", "#", "*#X", "*#X•▪"
     */
    String maskChars() default "";
    
    /**
     * Один символ для маскировки (альтернатива maskChars для простых случаев)
     * Если указан, будет использоваться вместо maskChars
     * Если не указан, используется maskChars или символ по умолчанию
     */
    char maskChar() default 0;
    
    /**
     * Начальная позиция для маскировки
     * Индекс символа, с которого начинается маскировка (0-based)
     * По умолчанию 4 (5-й символ)
     */
    int startIndex() default 4;
    
    /**
     * Длина маскируемой части
     * Количество символов для маскировки
     * По умолчанию 3 (5-й, 6-й, 7-й символы)
     */
    int length() default 3;
    
    /**
     * Количество символов для маскировки с начала
     * По умолчанию 8
     */
    int maskLength() default 8;
    
    /**
     * Количество символов для маскировки имени (маскируются только буквы)
     * Если > 0, используется специальная логика маскировки только букв
     * По умолчанию 0 (не используется)
     */
    int nameMaskLength() default 0;
}

