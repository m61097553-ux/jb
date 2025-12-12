package com.example.masker;

import org.springframework.stereotype.Service;

/**
 * Сервис для маскировки строковых значений
 */
@Service
public class DataMaskerService {
    
    private static final char[] DEFAULT_MASK_CHARS = {'*', '#', 'X', '•', '▪'};
    private char defaultMaskChar = '*';
    
    public void setDefaultMaskChar(char maskChar) {
        this.defaultMaskChar = maskChar;
    }
    
    /**
     * Маскирует значение на основе параметров
     * Использует общую логику без зависимости от типа
     */
    public String maskValue(String value, MaskingParams params) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        // Определяем символы для маскировки
        String maskCharsStr = determineMaskChars(params.maskChars(), params.maskChar());
        char maskChar = params.maskChar() != 0 ? params.maskChar() : defaultMaskChar;
        
        // Специальная логика для NAME (маскируем только буквы)
        if (params.nameMaskLength() > 0) {
            return maskName(value, maskChar, params.nameMaskLength());
        }
        
        // Определяем параметры маскировки на основе параметров (без switch по типу)
        int startIndex;
        int endIndex;
        boolean maskAll;
        
        // Если maskLength >= длины строки, то полная маскировка
        if (params.maskLength() > 0 && params.maskLength() >= value.length()) {
            startIndex = 0;
            endIndex = value.length();
            maskAll = true;
        }
        // Если указан startIndex и length, маскируем указанный диапазон
        else if (params.startIndex() >= 0 && params.length() > 0) {
            startIndex = params.startIndex();
            endIndex = Math.min(startIndex + params.length(), value.length());
            maskAll = false;
        }
        // Если указан maskLength, маскируем первые maskLength символов
        else if (params.maskLength() > 0) {
            startIndex = 0;
            endIndex = Math.min(params.maskLength(), value.length());
            maskAll = false;
        }
        // Иначе полная маскировка (по умолчанию, если не указаны другие параметры)
        else {
            startIndex = 0;
            endIndex = value.length();
            maskAll = true;
        }
        
        return mask(value, maskCharsStr, startIndex, endIndex, maskAll);
    }
    
    /**
     * Единая функция маскировки
     * 
     * @param value строка для маскировки
     * @param maskChars строка символов для маскировки (может быть один символ или несколько)
     * @param startIndex начальный индекс маскировки (включительно)
     * @param endIndex конечный индекс маскировки (исключительно)
     * @param maskAll флаг полной маскировки (если true, маскирует всю строку независимо от индексов)
     * @return замаскированная строка
     */
    public String mask(String value, String maskChars, int startIndex, int endIndex, boolean maskAll) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        // Определяем символы для маскировки
        String maskCharsStr = determineMaskChars(maskChars, (char) 0);
        if (maskCharsStr == null || maskCharsStr.isEmpty()) {
            maskCharsStr = String.valueOf(defaultMaskChar);
        }
        
        // Если полная маскировка, маскируем всю строку
        if (maskAll) {
            return maskRange(value, maskCharsStr, 0, value.length());
        }
        
        // Валидация индексов
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > value.length()) {
            endIndex = value.length();
        }
        if (startIndex >= endIndex) {
            return value;
        }
        
        // Маскируем указанный диапазон
        return maskRange(value, maskCharsStr, startIndex, endIndex);
    }
    
    /**
     * Маскирует указанный диапазон символов в строке
     */
    private String maskRange(String value, String maskChars, int startIndex, int endIndex) {
        StringBuilder sb = new StringBuilder(value);
        int maskCharsLength = maskChars.length();
        
        for (int i = startIndex; i < endIndex; i++) {
            sb.setCharAt(i, maskChars.charAt((i - startIndex) % maskCharsLength));
        }
        
        return sb.toString();
    }
    
    /**
     * Маскирует name - первые N букв имени маскируются указанным символом
     * Использует единую функцию mask для маскировки найденных букв
     */
    private String maskName(String name, char maskChar, int maskLength) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        
        StringBuilder sb = new StringBuilder(name);
        String maskCharStr = String.valueOf(maskChar);
        int maskedCount = 0;
        
        // Находим и маскируем первые N букв, используя единую функцию mask
        for (int i = 0; i < name.length() && maskedCount < maskLength; i++) {
            if (Character.isLetter(name.charAt(i))) {
                // Используем единую функцию mask для маскировки одного символа
                String masked = mask(String.valueOf(name.charAt(i)), maskCharStr, 0, 1, false);
                sb.setCharAt(i, masked.charAt(0));
                maskedCount++;
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Определяет строку символов для маскировки на основе параметров аннотации
     */
    private String determineMaskChars(String customMaskChars, char customMaskChar) {
        // Если указан один символ, используем его
        if (customMaskChar != 0) {
            return String.valueOf(customMaskChar);
        }
        
        // Если указана строка символов, используем её
        if (customMaskChars != null && !customMaskChars.isEmpty()) {
            return customMaskChars;
        }
        
        // Иначе используем символ по умолчанию
        return String.valueOf(defaultMaskChar);
    }
}

