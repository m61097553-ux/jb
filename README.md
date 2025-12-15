# HTTP Masking Library

Библиотека для маскировки чувствительных данных в теле HTTP запросов и ответов на основе конфигурации через properties.

## Возможности

- ✅ Рекурсивный поиск полей в JSON структуре
- ✅ Настройка маскировки через application.yml/properties
- ✅ Гибкая настройка символа маскировки, индексов и полной маскировки
- ✅ Поддержка вложенных объектов и массивов
- ✅ Маскировка как запросов, так и ответов
- ✅ Простая интеграция в Spring Boot приложения

## Установка

Добавьте зависимость в ваш `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>http-masking-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Настройка

### 1. Конфигурация через application.yml

```yaml
http:
  masking:
    request-enabled: true
    response-enabled: true
    default-mask-char: '*'
    fields:
      # Полная маскировка поля
      - field-name: password
        mask-all: true
        mask-char: '*'
      
      # Маскировка части поля (с 5 по 10 символ, индексы 4-9)
      - field-name: cardNumber
        mask-start-index: 4
        mask-end-index: 10
        mask-char: '#'
      
      # Маскировка первых 8 символов
      - field-name: inn
        mask-start-index: 0
        mask-end-index: 8
        mask-char: 'X'
```

### 2. Создание конфигурационного класса

Создайте класс конфигурации в вашем приложении:

```java
package com.example.config;

import com.example.masker.config.MaskingProperties;
import com.example.masker.filter.MaskingFilter;
import com.example.masker.service.JsonMaskingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MaskingProperties.class)
public class MaskingConfig {
    
    @Bean
    public JsonMaskingService jsonMaskingService(
            MaskingProperties properties, 
            ObjectMapper objectMapper) {
        return new JsonMaskingService(properties, objectMapper);
    }
    
    @Bean
    public MaskingFilter maskingFilter(
            MaskingProperties properties, 
            JsonMaskingService jsonMaskingService) {
        return new MaskingFilter(properties, jsonMaskingService);
    }
    
    @Bean
    public FilterRegistrationBean<MaskingFilter> maskingFilterRegistration(
            MaskingFilter filter) {
        FilterRegistrationBean<MaskingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("maskingFilter");
        return registration;
    }
}
```

## Параметры конфигурации

| Параметр | Тип | Описание | По умолчанию |
|----------|-----|----------|--------------|
| `http.masking.request-enabled` | boolean | Включить/выключить маскировку запросов | `true` |
| `http.masking.response-enabled` | boolean | Включить/выключить маскировку ответов | `true` |
| `http.masking.default-mask-char` | char | Символ маскировки по умолчанию | `*` |
| `http.masking.fields[].field-name` | String | Имя поля для маскировки (обязательно) | - |
| `http.masking.fields[].mask-char` | char | Символ маскировки для поля | Используется `default-mask-char` |
| `http.masking.fields[].mask-start-index` | int | Начальный индекс маскировки (0-based, включительно) | `0` |
| `http.masking.fields[].mask-end-index` | int | Конечный индекс маскировки (0-based, исключительно) | Длина строки |
| `http.masking.fields[].mask-all` | boolean | Флаг полной маскировки поля | `false` |

## Примеры конфигурации

### Пример 1: Полная маскировка нескольких полей

```yaml
http:
  masking:
    request-enabled: true
    response-enabled: true
    default-mask-char: '*'
    fields:
      - field-name: password
        mask-all: true
      - field-name: secretKey
        mask-all: true
        mask-char: '#'
      - field-name: apiKey
        mask-all: true
        mask-char: 'X'
```

### Пример 2: Маскировка только запросов

```yaml
http:
  masking:
    request-enabled: true
    response-enabled: false
    fields:
      - field-name: password
        mask-all: true
```

### Пример 3: Маскировка только ответов

```yaml
http:
  masking:
    request-enabled: false
    response-enabled: true
    fields:
      - field-name: token
        mask-all: true
```

### Пример 4: Маскировка части поля

```yaml
http:
  masking:
    fields:
      # Маскировка символов с 5 по 10 (индексы 4-9)
      - field-name: cardNumber
        mask-start-index: 4
        mask-end-index: 10
        mask-char: '#'
      
      # Маскировка первых 8 символов
      - field-name: inn
        mask-start-index: 0
        mask-end-index: 8
        mask-char: 'X'
      
      # Маскировка с 3 символа до конца
      - field-name: email
        mask-start-index: 3
        mask-char: '*'
```

### Пример 5: Рекурсивная маскировка вложенных объектов

Библиотека автоматически ищет поля рекурсивно во всех вложенных объектах и массивах.

**Входной JSON:**
```json
{
  "user": {
    "name": "John",
    "password": "secret123",
    "card": {
      "number": "1234567890123456",
      "cvv": "123"
    }
  },
  "items": [
    {
      "id": 1,
      "secret": "value1"
    },
    {
      "id": 2,
      "secret": "value2"
    }
  ]
}
```

**Конфигурация:**
```yaml
http:
  masking:
    fields:
      - field-name: password
        mask-all: true
      - field-name: number
        mask-start-index: 4
        mask-end-index: 12
        mask-char: '#'
      - field-name: secret
        mask-all: true
```

**Результат маскировки:**
```json
{
  "user": {
    "name": "John",
    "password": "********",
    "card": {
      "number": "1234########3456",
      "cvv": "123"
    }
  },
  "items": [
    {
      "id": 1,
      "secret": "******"
    },
    {
      "id": 2,
      "secret": "******"
    }
  ]
}
```

## Использование программно

Если вам нужно использовать сервис маскировки программно:

```java
@Autowired
private JsonMaskingService jsonMaskingService;

public void someMethod() {
    String json = "{\"password\":\"secret123\"}";
    String maskedJson = jsonMaskingService.maskJson(json);
    logger.debug("Masked JSON: {}", maskedJson);
}
```

## Отключение маскировки

Чтобы отключить маскировку запросов или ответов:

```yaml
http:
  masking:
    request-enabled: false
    response-enabled: false
```

Или удалите/закомментируйте конфигурационный класс.

## Требования

- Java 17+
- Spring Boot 3.1.0+
- Maven 3.6+

## Лицензия

MIT License
