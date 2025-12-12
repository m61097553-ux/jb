package com.example.masker.example;

import com.example.masker.DTOMaskingService;
import com.example.masker.example.dto.PaymentDTO;
import com.example.masker.example.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final DTOMaskingService dtoMaskingService;
    private final UserService userService;
    private final PaymentService paymentService;
    
    @GetMapping("/user")
    public UserDTO getUser() {
        UserDTO user = userService.getUser("123");
        String masked = dtoMaskingService.maskDTOToString(user);
        log.info("User DTO (masked): {}", masked);
        return user;
    }
    
    @GetMapping("/payment")
    public PaymentDTO getPayment() {
        PaymentDTO payment = paymentService.createPayment("PAY-001", 1000.0);
        String masked = dtoMaskingService.maskDTOToString(payment);
        log.info("Payment DTO (masked): {}", masked);
        return payment;
    }
}


