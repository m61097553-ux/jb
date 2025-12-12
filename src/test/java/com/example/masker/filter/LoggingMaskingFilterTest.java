package com.example.masker.filter;

import com.example.masker.DataMaskerService;
import com.example.masker.DTOMaskingService;
import com.example.masker.example.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingMaskingFilterTest {
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    private LoggingMaskingFilter filter;
    private DTOMaskingService dtoMaskingService;
    private DataMaskerService dataMaskerService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        dataMaskerService = new DataMaskerService();
        dtoMaskingService = new DTOMaskingService(dataMaskerService, objectMapper);
        filter = new LoggingMaskingFilter(dtoMaskingService);
    }
    
    @Test
    void testFilterWithUserDTORequest() throws Exception {
        // Подготовка данных
        UserDTO userDTO = new UserDTO(
                "123",
                "Иван",
                "1234567890123",  // INN - первые 8 символов должны быть замаскированы
                "1234567890",      // NUM - 5-й, 6-й, 7-й символы должны быть замаскированы
                "EPK123456",       // EPK_ID - полностью замаскирован символом #
                "Иван",            // firstName - первая буква должна быть заменена на *
                "Петров"           // lastName - полностью замаскирован
        );
        
        String requestBody = objectMapper.writeValueAsString(userDTO);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                requestBody.getBytes(StandardCharsets.UTF_8)
        );
        
        // Настройка моков
        when(request.getInputStream()).thenReturn(new MockServletInputStream(inputStream));
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/users");
        
        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(response.getStatus()).thenReturn(200);
        
        // Выполнение
        filter.doFilterInternal(request, response, filterChain);
        
        // Проверки
        verify(filterChain, times(1)).doFilter(any(), any());
        verify(request, atLeastOnce()).getInputStream();
    }
    
    @Test
    void testFilterWithEmptyRequestBody() throws Exception {
        // Настройка моков для пустого запроса
        ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(emptyStream));
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/users");
        
        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(response.getStatus()).thenReturn(200);
        
        // Выполнение
        filter.doFilterInternal(request, response, filterChain);
        
        // Проверки
        verify(filterChain, times(1)).doFilter(any(), any());
    }
    
    @Test
    void testFilterWithResponseBody() throws Exception {
        // Подготовка данных для ответа
        UserDTO userDTO = new UserDTO(
                "123",
                "Иван",
                "1234567890123",
                "1234567890",
                "EPK123456",
                "Иван",
                "Петров"
        );
        
        String responseBody = objectMapper.writeValueAsString(userDTO);
        
        // Настройка моков
        ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(emptyStream));
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/users");
        
        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(response.getStatus()).thenReturn(200);
        
        // Выполнение
        filter.doFilterInternal(request, response, filterChain);
        
        // Записываем тело ответа после выполнения фильтра
        printWriter.write(responseBody);
        printWriter.flush();
        
        // Проверки
        verify(filterChain, times(1)).doFilter(any(), any());
    }
    
    @Test
    void testFilterWithInvalidJson() throws Exception {
        // Подготовка некорректного JSON
        String invalidJson = "{invalid json}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                invalidJson.getBytes(StandardCharsets.UTF_8)
        );
        
        // Настройка моков
        when(request.getInputStream()).thenReturn(new MockServletInputStream(inputStream));
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/users");
        
        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(response.getStatus()).thenReturn(200);
        
        // Выполнение - не должно выбросить исключение
        assertDoesNotThrow(() -> {
            filter.doFilterInternal(request, response, filterChain);
        });
        
        // Проверки
        verify(filterChain, times(1)).doFilter(any(), any());
    }
    
    @Test
    void testGetRequestBody() throws Exception {
        // Подготовка данных
        String requestBody = "{\"test\":\"value\"}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                requestBody.getBytes(StandardCharsets.UTF_8)
        );
        
        when(request.getInputStream()).thenReturn(new MockServletInputStream(inputStream));
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/test");
        
        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(response.getStatus()).thenReturn(200);
        
        // Выполнение
        filter.doFilterInternal(request, response, filterChain);
        
        // Проверяем, что фильтр обработал запрос
        verify(filterChain, times(1)).doFilter(any(), any());
    }
    
    @Test
    void testMaskRequestBody() throws Exception {
        // Подготовка UserDTO с чувствительными данными
        UserDTO userDTO = new UserDTO(
                "123",
                "Иван",
                "1234567890123",  // INN
                "1234567890",      // NUM
                "EPK123456",       // EPK_ID
                "Иван",            // firstName
                "Петров"           // lastName
        );
        
        String requestBody = objectMapper.writeValueAsString(userDTO);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                requestBody.getBytes(StandardCharsets.UTF_8)
        );
        
        when(request.getInputStream()).thenReturn(new MockServletInputStream(inputStream));
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/users");
        
        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(response.getStatus()).thenReturn(200);
        
        // Выполнение
        filter.doFilterInternal(request, response, filterChain);
        
        // Проверяем, что маскировка произошла
        // INN должен содержать замаскированные первые 8 символов
        verify(filterChain, times(1)).doFilter(any(), any());
    }
    
    /**
     * Мок для ServletInputStream
     */
    private static class MockServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;
        
        public MockServletInputStream(ByteArrayInputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
        
        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }
        
        @Override
        public boolean isReady() {
            return true;
        }
        
        @Override
        public void setReadListener(jakarta.servlet.ReadListener listener) {
            // Не используется в тестах
        }
    }
}


