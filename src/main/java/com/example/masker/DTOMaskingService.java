package com.example.masker;

import com.example.masker.annotation.Mask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Сервис для маскировки полей DTO при логировании
 * Можно включить/выключить через конфигурацию: data.masker.enabled=true/false
 */
@Service
@ConditionalOnProperty(prefix = "data.masker", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DataMaskerProperties.class)
public class DTOMaskingService {
    
    private final DataMaskerService dataMaskerService;
    
    @Autowired(required = false)
    private ObjectMapper objectMapper;
    
    // Конструктор для тестирования (Lombok создаст конструктор только для final полей)
    public DTOMaskingService(DataMaskerService dataMaskerService, ObjectMapper objectMapper) {
        this.dataMaskerService = dataMaskerService;
        this.objectMapper = objectMapper;
    }
    
    private ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }
    
    /**
     * Публичный метод для получения ObjectMapper (для использования в фильтрах)
     */
    public ObjectMapper getMapper() {
        return getObjectMapper();
    }
    
    /**
     * Преобразует DTO в строку с замаскированными полями для логирования
     */
    public String maskDTOToString(Object dto) {
        if (dto == null) {
            return "null";
        }
        
        try {
            ObjectMapper mapper = getObjectMapper();
            // Преобразуем объект в JSON
            String json = mapper.writeValueAsString(dto);
            JsonNode jsonNode = mapper.readTree(json);
            
            // Маскируем JSON на основе аннотаций полей
            JsonNode maskedNode = maskJsonNode(jsonNode, dto.getClass());
            
            // Преобразуем обратно в строку
            return mapper.writeValueAsString(maskedNode);
        } catch (Exception e) {
            return dto.toString();
        }
    }
    
    /**
     * Рекурсивно маскирует JSON узел на основе аннотаций @Mask в классе
     */
    private JsonNode maskJsonNode(JsonNode node, Class<?> clazz) {
        if (node == null || node.isNull()) {
            return node;
        }
        
        ObjectMapper mapper = getObjectMapper();
        
        if (node.isObject()) {
            ObjectNode maskedObject = mapper.createObjectNode();
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                
                Mask maskAnnotation = findMaskAnnotation(fieldName, clazz);
                
                if (maskAnnotation != null && fieldValue.isTextual()) {
                    MaskingParams params = MaskingParams.fromAnnotation(maskAnnotation);
                    String maskedValue = dataMaskerService.maskValue(fieldValue.asText(), params);
                    maskedObject.put(fieldName, maskedValue);
                } else if (fieldValue.isObject() || fieldValue.isArray()) {
                    maskedObject.set(fieldName, maskJsonNode(fieldValue, clazz));
                } else {
                    maskedObject.set(fieldName, fieldValue);
                }
            });
            return maskedObject;
            
        } else if (node.isArray()) {
            ArrayNode maskedArray = mapper.createArrayNode();
            for (JsonNode element : node) {
                if (element.isObject() || element.isArray()) {
                    maskedArray.add(maskJsonNode(element, clazz));
                } else {
                    maskedArray.add(element);
                }
            }
            return maskedArray;
        }
        
        return node;
    }
    
    /**
     * Ищет аннотацию @Mask для поля по имени в классе
     */
    private Mask findMaskAnnotation(String fieldName, Class<?> clazz) {
        Class<?> currentClass = clazz;
        
        while (currentClass != null && currentClass != Object.class) {
            try {
                java.lang.reflect.Field field = currentClass.getDeclaredField(fieldName);
                return field.getAnnotation(Mask.class);
            } catch (NoSuchFieldException e) {
                // Продолжаем поиск в суперклассе
                currentClass = currentClass.getSuperclass();
            }
        }
        
        return null;
    }
}

