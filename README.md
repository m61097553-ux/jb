# Data Masker Spring Boot Starter

Spring Boot Starter для маскировки чувствительных данных в полях DTO при логировании.

## Возможности

- ✅ Маскировка полей DTO через аннотацию `@Mask`
- ✅ Автоматическая маскировка HTTP запросов и ответов через фильтр
- ✅ Поддержка разных символов маски в разных позициях строки
- ✅ Настраиваемая конфигурация через properties
- ✅ Минимальное использование рефлексии

## Установка

Добавьте зависимость в ваш `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>data-masker-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Конфигурация

В `application.yml`:

```yaml
data:
  masker:
    mask-char: '*'              # Символ для маскировки по умолчанию
    enable-auto-masking: true   # Включить автоматическую маскировку
```

## Использование

### 1. Создайте DTO с аннотациями @Mask

```java
public class UserDTO {
    
    @Mask(value = Mask.MaskType.EMAIL)
    private String email;
    
    // Использование одного символа через maskChar
    @Mask(value = Mask.MaskType.PASSWORD, maskChar = '#')
    private String password;
    
    // Использование нескольких символов через maskChars
    @Mask(value = Mask.MaskType.CARD_NUMBER, maskChars = "*#X•")
    private String cardNumber;
    
    // Без указания символов - используется символ по умолчанию из конфигурации
    @Mask(value = Mask.MaskType.PHONE)
    private String phone;
}
```

### 2. Используйте DTOMaskingService для логирования

```java
@Service
public class UserService {
    
    @Autowired
    private DTOMaskingService dtoMaskingService;
    
    public void createUser(UserDTO userDTO) {
        String maskedDTO = dtoMaskingService.maskDTOToString(userDTO);
        log.info("Создание пользователя: {}", maskedDTO);
    }
}
```

### 3. Автоматическая маскировка HTTP запросов/ответов

`DTOMaskingFilter` автоматически маскирует DTO в HTTP запросах и ответах:

**Результат в логах:**
```
HTTP Request: POST /api/users | Body: {"email":"jo*#@example.com","password":"se*#X•***23",...} | Execution time: 45ms
HTTP Response: Status 200 | Body: {"id":"user123","email":"jo*#@example.com",...} | Execution time: 45ms
```

## Доступные типы маскировки

- `PASSWORD` - маскирует пароль
- `CARD_NUMBER` - маскирует номер карты
- `EMAIL` - маскирует email
- `PHONE` - маскирует телефон
- `STRING` - маскирует строку
- `ALL` - полностью маскирует
- `PRESERVE_FORMAT` - маскирует с сохранением формата
- `NUM` - маскирует с 5 по 7 символ (индексы 4, 5, 6)
- `EPK_ID` - маскирует полностью
- `INN` - маскирует первые 8 символов
- `NAME` - первая буква имени маскируется точкой
- `SURNAME` - маскирует полностью

### Примеры специфичных типов маскировки

```java
public class ExampleDTO {
    
    @Mask(value = Mask.MaskType.NUM)
    private String num;  // "1234567890" -> "1234*#X7890" (5-й, 6-й, 7-й символы)
    
    @Mask(value = Mask.MaskType.EPK_ID)
    private String epkId;  // "EPK123456" -> "*#X•▪*#X•" (полностью)
    
    @Mask(value = Mask.MaskType.INN)
    private String inn;  // "1234567890123" -> "*#X•▪*#X•890123" (первые 8 символов)
    
    @Mask(value = Mask.MaskType.NAME)
    private String name;  // "John" -> ".ohn" (первая буква точкой)
    
    @Mask(value = Mask.MaskType.SURNAME)
    private String surname;  // "Doe" -> "*#X" (полностью)
}
```

## Требования

- Java 17+
- Spring Boot 3.1.0+

## Лицензия

MIT

