package com.example.masker.example;

import com.example.masker.DTOMaskingService;
import com.example.masker.example.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Пример сервиса с использованием маскировки DTO при логировании
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final DTOMaskingService dtoMaskingService;
    
    /**
     * Пример создания пользователя с логированием замаскированного DTO
     */
    public void createUser(UserDTO userDTO) {
        String maskedDTO = dtoMaskingService.maskDTOToString(userDTO);
        log.info("Создание пользователя: {}", maskedDTO);
    }
    
    /**
     * Пример получения пользователя
     */
    public UserDTO getUser(String id) {
        return new UserDTO(
                id,
                "Иван",
                "1234567890123",
                "1234567890",
                "EPK123456",
                "Иван",
                "Петров"
        );
    }
}

