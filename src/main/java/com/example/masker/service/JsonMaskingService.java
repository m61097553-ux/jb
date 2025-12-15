package com.example.masker.service;

import com.example.masker.config.MaskingProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для маскировки данных в JSON на основе конфигурации
 */
@RequiredArgsConstructor
public class JsonMaskingService {
    
    private static final String CODE_FIELD = "code";
    private static final String CODE_VALUE_FIELD = "codeValue";
    
    private final MaskingProperties properties;
    private final ObjectMapper objectMapper;
    private Map<String, MaskingProperties.FieldMaskingConfig> fieldConfigMap;
    
    /**
     * Маскирует JSON строку на основе конфигурации полей
     */
    public String maskJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        
        // Инициализируем Map для быстрого поиска конфигураций
        initializeFieldConfigMap();
        
        // Если нет полей для маскировки, возвращаем исходную строку
        if (fieldConfigMap.isEmpty()) {
            return json;
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode maskedNode = maskJsonNode(rootNode);
            return objectMapper.writeValueAsString(maskedNode);
        } catch (Exception e) {
            // Если не удалось распарсить JSON, возвращаем исходную строку
            return json;
        }
    }
    
    /**
     * Инициализирует Map для быстрого поиска конфигураций полей
     */
    private void initializeFieldConfigMap() {
        if (fieldConfigMap == null) {
            fieldConfigMap = new HashMap<>();
            for (MaskingProperties.FieldMaskingConfig config : properties.getFields()) {
                if (config.getFieldName() != null) {
                    fieldConfigMap.put(config.getFieldName(), config);
                }
            }
        }
    }
    
    /**
     * Рекурсивно маскирует JSON узел
     */
    private JsonNode maskJsonNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return node;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;

            // Если это «динамическое» DTO с полями code / codeValue – обрабатываем отдельно
            if (isDynamicDto(objectNode)) {
                return maskDynamicDtoNode(objectNode);
            }

            // Обычный POJO-объект
            return maskPojoNode(objectNode);

        } else if (node.isArray()) {
            ArrayNode maskedArray = objectMapper.createArrayNode();
            for (JsonNode element : node) {
                if (element.isObject() || element.isArray()) {
                    maskedArray.add(maskJsonNode(element));
                } else {
                    maskedArray.add(element);
                }
            }
            return maskedArray;
        }

        return node;
    }

    /**
     * Маскировка обычных POJO-объектов (без dynamic code / codeValue логики)
     */
    private JsonNode maskPojoNode(ObjectNode node) {
        ObjectNode maskedObject = objectMapper.createObjectNode();

        var fieldsIterator = node.fields();
        while (fieldsIterator.hasNext()) {
            var entry = fieldsIterator.next();
            String fieldName = entry.getKey();
            JsonNode fieldValue = entry.getValue();

            // Проверяем, нужно ли маскировать это поле (быстрый поиск через Map)
            MaskingProperties.FieldMaskingConfig config = fieldConfigMap.get(fieldName);

            if (config != null && fieldValue.isTextual()) {
                // Маскируем текстовое значение
                String maskedValue = maskValue(fieldValue.asText(), config);
                maskedObject.put(fieldName, maskedValue);
            } else if (fieldValue.isObject() || fieldValue.isArray()) {
                // Рекурсивно обрабатываем вложенные объекты и массивы
                maskedObject.set(fieldName, maskJsonNode(fieldValue));
            } else {
                // Оставляем значение без изменений
                maskedObject.set(fieldName, fieldValue);
            }
        }

        return maskedObject;
    }

    /**
     * Маскировка «динамических» DTO, где схема задаётся полем code, а значение — в codeValue.
     */
    private JsonNode maskDynamicDtoNode(ObjectNode node) {
        ObjectNode maskedObject = objectMapper.createObjectNode();

        // Определяем конфигурацию для пары code / codeValue
        String codeValue = null;
        MaskingProperties.FieldMaskingConfig codeValueConfig = null;

        JsonNode codeNode = node.get(CODE_FIELD);
        JsonNode codeValueNode = node.get(CODE_VALUE_FIELD);

        if (codeNode != null && codeNode.isTextual() && codeValueNode != null && codeValueNode.isTextual()) {
            String codeFieldName = codeNode.asText();
            MaskingProperties.FieldMaskingConfig config = fieldConfigMap.get(codeFieldName);
            if (config != null) {
                codeValue = codeValueNode.asText();
                codeValueConfig = config;
            }
        }

        // Обрабатываем все поля
        var fieldsIterator = node.fields();
        while (fieldsIterator.hasNext()) {
            var entry = fieldsIterator.next();
            String fieldName = entry.getKey();
            JsonNode fieldValue = entry.getValue();

            // Специальная обработка для codeValue
            if (CODE_VALUE_FIELD.equals(fieldName) && codeValueConfig != null && codeValue != null) {
                String maskedValue = maskValue(codeValue, codeValueConfig);
                maskedObject.put(fieldName, maskedValue);
                continue;
            }

            // Пропускаем code - копируем как есть
            if (CODE_FIELD.equals(fieldName)) {
                maskedObject.set(fieldName, fieldValue);
                continue;
            }

            // Для всех остальных полей используем обычную логику POJO
            MaskingProperties.FieldMaskingConfig config = fieldConfigMap.get(fieldName);
            if (config != null && fieldValue.isTextual()) {
                String maskedValue = maskValue(fieldValue.asText(), config);
                maskedObject.put(fieldName, maskedValue);
            } else if (fieldValue.isObject() || fieldValue.isArray()) {
                maskedObject.set(fieldName, maskJsonNode(fieldValue));
            } else {
                maskedObject.set(fieldName, fieldValue);
            }
        }

        return maskedObject;
    }

    /**
     * Проверяет, является ли объект «динамическим» DTO с полями code и codeValue.
     */
    private boolean isDynamicDto(ObjectNode node) {
        return node.has(CODE_FIELD) && node.has(CODE_VALUE_FIELD);
    }
    
    /**
     * Маскирует значение строки на основе конфигурации
     */
    private String maskValue(String value, MaskingProperties.FieldMaskingConfig config) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        char maskChar = config.getMaskChar() != null 
                ? config.getMaskChar() 
                : properties.getDefaultMaskChar();
        
        // Полная маскировка
        if (config.isMaskAll()) {
            return maskString(value, maskChar, 0, value.length());
        }
        
        // Маскировка по индексам.
        // Здесь индексы трактуются как 0-based:
        //  - maskStartIndex: индекс первого символа для маскирования (включительно)
        //  - maskEndIndex:   индекс окончания маскирования (исключительно)
        // Такой подход согласован с комментариями в тестах
        Integer cfgStart = config.getMaskStartIndex();
        Integer cfgEnd = config.getMaskEndIndex();

        int startIndex = cfgStart != null
                ? Math.max(0, cfgStart)
                : 0;

        int endIndex = cfgEnd != null
                ? Math.min(value.length(), cfgEnd)
                : value.length();

        // Валидация индексов
        if (startIndex >= endIndex || startIndex >= value.length()) {
            return value;
        }
        
        return maskString(value, maskChar, startIndex, endIndex);
    }
    
    /**
     * Маскирует часть строки от startIndex до endIndex
     * Оптимизировано для производительности
     */
    private String maskString(String value, char maskChar, int startIndex, int endIndex) {
        int length = value.length();
        int actualEnd = Math.min(endIndex, length);
        
        if (startIndex >= actualEnd) {
            return value;
        }
        
        // Используем char[] для более эффективной работы со строками
        char[] chars = value.toCharArray();
        for (int i = startIndex; i < actualEnd; i++) {
            chars[i] = maskChar;
        }
        return new String(chars);
    }
}

