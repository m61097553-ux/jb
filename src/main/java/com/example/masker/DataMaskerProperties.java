package com.example.masker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационные свойства для Data Masker
 */
@Configuration
@ConfigurationProperties(prefix = "data.masker")
@Data
public class DataMaskerProperties {
    
    /**
     * Включить автоматическую маскировку DTO
     */
    private boolean enabled = true;
}

