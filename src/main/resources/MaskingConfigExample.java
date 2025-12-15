package com.example.config;

import com.example.masker.config.MaskingProperties;
import com.example.masker.filter.MaskingFilter;
import com.example.masker.service.JsonMaskingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Пример конфигурационного класса для использования библиотеки http-masking-library
 * 
 * Скопируйте этот класс в ваш проект и настройте под свои нужды.
 */
@Configuration
@EnableConfigurationProperties(MaskingProperties.class)
public class MaskingConfigExample {
    
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

