package com.example.masker.integration;

import com.example.masker.config.MaskingProperties;
import com.example.masker.service.JsonMaskingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для JsonMaskingService
 * Конфигурация загружается из application-test.yml
 */
@SpringBootTest(classes = JsonMaskingServiceIntegrationTest.TestConfiguration.class)
@EnableConfigurationProperties(MaskingProperties.class)
public class JsonMaskingServiceIntegrationTest {
    
    @Configuration
    static class TestConfiguration {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
        
        @Bean
        public MaskingProperties maskingProperties() {
            MaskingProperties properties = new MaskingProperties();
            
            // Загружаем конфигурацию из application-test.yml
            try {
                ClassPathResource resource = new ClassPathResource("application-test.yml");
                try (InputStream inputStream = resource.getInputStream()) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(inputStream);
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> http = (Map<String, Object>) data.get("http");
                    if (http != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> masking = (Map<String, Object>) http.get("masking");
                        if (masking != null) {
                            if (masking.containsKey("request-enabled")) {
                                properties.setRequestEnabled((Boolean) masking.get("request-enabled"));
                            }
                            if (masking.containsKey("response-enabled")) {
                                properties.setResponseEnabled((Boolean) masking.get("response-enabled"));
                            }
                            if (masking.containsKey("default-mask-char")) {
                                String maskChar = String.valueOf(masking.get("default-mask-char"));
                                if (maskChar != null && !maskChar.isEmpty() && !maskChar.equals("null")) {
                                    properties.setDefaultMaskChar(maskChar.charAt(0));
                                }
                            }
                            if (masking.containsKey("fields")) {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> fields = (List<Map<String, Object>>) masking.get("fields");
                                if (fields != null) {
                                    for (Map<String, Object> fieldConfig : fields) {
                                        MaskingProperties.FieldMaskingConfig config = 
                                            new MaskingProperties.FieldMaskingConfig();
                                        config.setFieldName((String) fieldConfig.get("field-name"));
                                        if (fieldConfig.containsKey("mask-char")) {
                                            String maskChar = String.valueOf(fieldConfig.get("mask-char"));
                                            if (maskChar != null && !maskChar.isEmpty() && !maskChar.equals("null")) {
                                                config.setMaskChar(maskChar.charAt(0));
                                            }
                                        }
                                        if (fieldConfig.containsKey("mask-start-index")) {
                                            config.setMaskStartIndex((Integer) fieldConfig.get("mask-start-index"));
                                        }
                                        if (fieldConfig.containsKey("mask-end-index")) {
                                            config.setMaskEndIndex((Integer) fieldConfig.get("mask-end-index"));
                                        }
                                        if (fieldConfig.containsKey("mask-all")) {
                                            config.setMaskAll((Boolean) fieldConfig.get("mask-all"));
                                        }
                                        properties.getFields().add(config);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load application-test.yml", e);
            }
            
            return properties;
        }
        
        @Bean
        public JsonMaskingService jsonMaskingService(MaskingProperties properties, ObjectMapper objectMapper) {
            return new JsonMaskingService(properties, objectMapper);
        }
    }
    
    @Autowired
    private JsonMaskingService jsonMaskingService;
    
    @BeforeEach
    void setUp() {
        // Убеждаемся, что сервис загружен
        assertNotNull(jsonMaskingService, "JsonMaskingService should not be null");
    }
    
    @Test
    void testFullMasking() {
        String json = "{\"username\":\"john\",\"password\":\"secret123\"}";
        String result = jsonMaskingService.maskJson(json);
        
        assertTrue(result.contains("\"password\":\"********\""));
        assertTrue(result.contains("\"username\":\"john\""));
    }
    
    @Test
    void testPartialMasking() {
        String json = "{\"cardNumber\":\"1234567890123456\"}";
        String result = jsonMaskingService.maskJson(json);
        
        // Маскировка с 4 по 10 индекс (символы 5-10)
        assertTrue(result.contains("\"cardNumber\":\"1234######0123456\""));
    }
    
    @Test
    void testNestedObjects() {
        String json = "{\"user\":{\"name\":\"John\",\"password\":\"secret123\"}}";
        String result = jsonMaskingService.maskJson(json);
        
        assertTrue(result.contains("\"password\":\"********\""));
        assertTrue(result.contains("\"name\":\"John\""));
    }
    
    @Test
    void testArrays() {
        String json = "{\"items\":[{\"id\":1,\"password\":\"pass1\"},{\"id\":2,\"password\":\"pass2\"}]}";
        String result = jsonMaskingService.maskJson(json);
        
        assertTrue(result.contains("\"password\":\"*****\""));
        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"id\":2"));
    }
    
    @Test
    void testMultipleFields() {
        String json = "{\"username\":\"john\",\"password\":\"secret123\",\"cardNumber\":\"1234567890123456\"}";
        String result = jsonMaskingService.maskJson(json);
        
        assertTrue(result.contains("\"password\":\"********\""));
        assertTrue(result.contains("\"cardNumber\":\"1234######0123456\""));
        assertTrue(result.contains("\"username\":\"john\""));
    }
    
    @Test
    void testCodeCodeValuePattern() {
        String json = "{\"code\":\"inn\",\"codeValue\":\"455444343\"}";
        String result = jsonMaskingService.maskJson(json);
        
        // codeValue должен быть замаскирован, так как code="inn" есть в конфигурации
        assertTrue(result.contains("\"codeValue\":\"*********\""));
        assertTrue(result.contains("\"code\":\"inn\""));
    }
    
    @Test
    void testCodeCodeValuePattern_WithOtherFields() {
        String json = "{\"code\":\"inn\",\"codeValue\":\"455444343\",\"username\":\"john\",\"password\":\"secret123\"}";
        String result = jsonMaskingService.maskJson(json);
        
        // codeValue должен быть замаскирован
        assertTrue(result.contains("\"codeValue\":\"*********\""));
        // password должен быть замаскирован (прямое совпадение)
        assertTrue(result.contains("\"password\":\"********\""));
        // username должен остаться без изменений
        assertTrue(result.contains("\"username\":\"john\""));
        assertTrue(result.contains("\"code\":\"inn\""));
    }
    
    @Test
    void testCodeCodeValuePattern_Nested() {
        String json = "{\"user\":{\"code\":\"inn\",\"codeValue\":\"455444343\"},\"name\":\"John\"}";
        String result = jsonMaskingService.maskJson(json);
        
        // codeValue во вложенном объекте должен быть замаскирован
        assertTrue(result.contains("\"codeValue\":\"*********\""));
        assertTrue(result.contains("\"code\":\"inn\""));
        assertTrue(result.contains("\"name\":\"John\""));
    }
    
    @Test
    void testCodeCodeValuePattern_InArray() {
        String json = "{\"items\":[{\"code\":\"inn\",\"codeValue\":\"111111111\"},{\"code\":\"inn\",\"codeValue\":\"222222222\"}]}";
        String result = jsonMaskingService.maskJson(json);
        
        // Оба codeValue в массиве должны быть замаскированы
        assertTrue(result.contains("\"codeValue\":\"*********\""));
        assertTrue(result.contains("\"code\":\"inn\""));
    }
    
    @Test
    void testCodeCodeValuePattern_NotInConfig() {
        String json = "{\"code\":\"unknown\",\"codeValue\":\"455444343\"}";
        String result = jsonMaskingService.maskJson(json);
        
        // codeValue НЕ должен быть замаскирован, так как unknown нет в конфигурации
        assertTrue(result.contains("\"codeValue\":\"455444343\""));
        assertTrue(result.contains("\"code\":\"unknown\""));
    }
    
    @Test
    void testNestedJsonInStringValue() {
        String json = "{\"data\":\"{\\\"username\\\":\\\"john\\\",\\\"password\\\":\\\"secret123\\\"}\"}";
        String result = jsonMaskingService.maskJson(json);
        
        // password во вложенном JSON должен быть замаскирован
        assertTrue(result.contains("\"password\":\"********\""));
        assertTrue(result.contains("\"username\":\"john\""));
    }
    
    @Test
    void testNestedJsonArrayInStringValue() {
        String json = "{\"items\":\"[{\\\"id\\\":1,\\\"password\\\":\\\"value1\\\"},{\\\"id\\\":2,\\\"password\\\":\\\"value2\\\"}]\"}";
        String result = jsonMaskingService.maskJson(json);
        
        // password в вложенном JSON массиве должен быть замаскирован
        assertTrue(result.contains("\"password\":\"*****\""));
        assertTrue(result.contains("\"id\":1"));
    }
    
    @Test
    void testComplexScenario_AllFeatures() {
        // Комплексный JSON со всеми функциями
        String json = "{\"user\":{\"code\":\"inn\",\"codeValue\":\"455444343\",\"password\":\"secret123\",\"cardNumber\":\"1234567890123456\"},\"data\":\"{\\\"password\\\":\\\"nested123\\\"}\",\"items\":[{\"code\":\"inn\",\"codeValue\":\"111111111\"}]}";
        String result = jsonMaskingService.maskJson(json);
        
        // Проверяем все типы маскировки
        assertTrue(result.contains("\"codeValue\":\"*********\""));
        assertTrue(result.contains("\"password\":\"********\""));
        assertTrue(result.contains("\"cardNumber\":\"1234######0123456\""));
        // Проверяем вложенный JSON в строке
        assertTrue(result.contains("\"password\":\"********\""));
    }
    
    @Test
    void testCodeCodeValuePattern_PartialMasking() {
        String json = "{\"code\":\"inn-partial\",\"codeValue\":\"455444343\"}";
        String result = jsonMaskingService.maskJson(json);
        
        // codeValue должен быть частично замаскирован (первые 8 символов)
        assertTrue(result.contains("\"codeValue\":\"########3\""));
        assertTrue(result.contains("\"code\":\"inn-partial\""));
    }
    
    @Test
    void testMultipleCodeCodeValuePatterns() {
        String json = "{\"field1\":{\"code\":\"inn\",\"codeValue\":\"111111111\"},\"field2\":{\"code\":\"snils\",\"codeValue\":\"222222222\"}}";
        String result = jsonMaskingService.maskJson(json);
        
        // Оба codeValue должны быть замаскированы разными символами
        assertTrue(result.contains("\"codeValue\":\"*********\""));
        assertTrue(result.contains("\"codeValue\":\"#########\""));
    }
    
    @Test
    void testEmptyJson() {
        String json = "{}";
        String result = jsonMaskingService.maskJson(json);
        
        assertEquals("{}", result);
    }
    
    @Test
    void testInvalidJson() {
        String invalidJson = "{invalid json}";
        String result = jsonMaskingService.maskJson(invalidJson);
        
        // При невалидном JSON должна вернуться исходная строка
        assertEquals(invalidJson, result);
    }
    
    @Test
    void testNullOrEmptyString() {
        assertNull(jsonMaskingService.maskJson(null));
        assertEquals("", jsonMaskingService.maskJson(""));
    }
    
    @Test
    void testNonConfiguredFields() {
        String json = "{\"username\":\"john\",\"email\":\"john@example.com\"}";
        String result = jsonMaskingService.maskJson(json);
        
        // Поля, не указанные в конфигурации, не должны маскироваться
        assertTrue(result.contains("\"username\":\"john\""));
        assertTrue(result.contains("\"email\":\"john@example.com\""));
    }
}
