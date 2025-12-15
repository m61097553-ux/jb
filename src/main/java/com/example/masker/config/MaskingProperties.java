package com.example.masker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Конфигурационные свойства для маскировки данных в HTTP запросах и ответах
 */
@Data
@ConfigurationProperties(prefix = "http.masking")
public class MaskingProperties {
    
    /**
     * Включена ли маскировка запросов
     */
    private boolean requestEnabled = true;
    
    /**
     * Включена ли маскировка ответов
     */
    private boolean responseEnabled = true;
    
    /**
     * Список полей для маскировки
     */
    private List<FieldMaskingConfig> fields = new ArrayList<>();
    
    /**
     * Символ маскировки по умолчанию
     */
    private char defaultMaskChar = '*';
    
    /**
     * Конфигурация маскировки для конкретного поля
     */
    @Data
    public static class FieldMaskingConfig {
        
        /**
         * Имя поля для поиска в JSON (поддерживается рекурсивный поиск)
         */
        private String fieldName;
        
        /**
         * Символ маскировки (если не указан, используется defaultMaskChar)
         */
        private Character maskChar;
        
        /**
         * Начальный индекс для маскировки (включительно, 0-based)
         * Если не указан, используется полная маскировка или maskStartIndex = 0
         */
        private Integer maskStartIndex;
        
        /**
         * Конечный индекс для маскировки (исключительно, 0-based)
         * Если не указан, используется полная маскировка или maskEndIndex = длина строки
         */
        private Integer maskEndIndex;
        
        /**
         * Флаг полной маскировки поля
         * Если true, маскируется всё поле независимо от индексов
         */
        private boolean maskAll = false;
    }
}

