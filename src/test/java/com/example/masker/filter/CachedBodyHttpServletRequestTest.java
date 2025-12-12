package com.example.masker.filter;

import com.example.masker.DataMaskerService;
import com.example.masker.DTOMaskingService;
import com.example.masker.example.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CachedBodyHttpServletRequestTest {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void testGetBodyAsString() throws IOException {
        // Подготовка данных
        UserDTO userDTO = new UserDTO("123", "Иван", "1234567890123", "1234567890", 
                "EPK123456", "Иван", "Петров");
        String json = objectMapper.writeValueAsString(userDTO);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                json.getBytes(StandardCharsets.UTF_8)
        );
        
        // Создание мокового request
        jakarta.servlet.http.HttpServletRequest mockRequest = 
                new jakarta.servlet.http.HttpServletRequest() {
                    @Override
                    public jakarta.servlet.ServletInputStream getInputStream() throws IOException {
                        return new MockServletInputStream(inputStream);
                    }
                    
                    // Остальные методы возвращают значения по умолчанию
                    @Override public String getAuthType() { return null; }
                    @Override public jakarta.servlet.http.Cookie[] getCookies() { return new jakarta.servlet.http.Cookie[0]; }
                    @Override public long getDateHeader(String name) { return 0; }
                    @Override public String getHeader(String name) { return null; }
                    @Override public java.util.Enumeration<String> getHeaders(String name) { return null; }
                    @Override public java.util.Enumeration<String> getHeaderNames() { return null; }
                    @Override public int getIntHeader(String name) { return 0; }
                    @Override public String getMethod() { return "POST"; }
                    @Override public String getPathInfo() { return null; }
                    @Override public String getPathTranslated() { return null; }
                    @Override public String getContextPath() { return ""; }
                    @Override public String getQueryString() { return null; }
                    @Override public String getRemoteUser() { return null; }
                    @Override public boolean isUserInRole(String role) { return false; }
                    @Override public java.security.Principal getUserPrincipal() { return null; }
                    @Override public String getRequestedSessionId() { return null; }
                    @Override public String getRequestURI() { return "/api/test"; }
                    @Override public StringBuffer getRequestURL() { return new StringBuffer("http://localhost/api/test"); }
                    @Override public String getServletPath() { return "/api"; }
                    @Override public jakarta.servlet.http.HttpSession getSession(boolean create) { return null; }
                    @Override public jakarta.servlet.http.HttpSession getSession() { return null; }
                    @Override public String changeSessionId() { return null; }
                    @Override public boolean isRequestedSessionIdValid() { return false; }
                    @Override public boolean isRequestedSessionIdFromCookie() { return false; }
                    @Override public boolean isRequestedSessionIdFromURL() { return false; }
                    @Override public boolean isRequestedSessionIdFromUrl() { return false; }
                    @Override public boolean authenticate(jakarta.servlet.http.HttpServletResponse response) throws IOException, jakarta.servlet.ServletException { return false; }
                    @Override public void login(String username, String password) throws jakarta.servlet.ServletException {}
                    @Override public void logout() throws jakarta.servlet.ServletException {}
                    @Override public java.util.Collection<jakarta.servlet.http.Part> getParts() throws IOException, jakarta.servlet.ServletException { return null; }
                    @Override public jakarta.servlet.http.Part getPart(String name) throws IOException, jakarta.servlet.ServletException { return null; }
                    @Override public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, jakarta.servlet.ServletException { return null; }
                    @Override public java.util.Enumeration<String> getAttributeNames() { return null; }
                    @Override public Object getAttribute(String name) { return null; }
                    @Override public void setAttribute(String name, Object o) {}
                    @Override public void removeAttribute(String name) {}
                    @Override public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
                    @Override public java.util.Enumeration<java.util.Locale> getLocales() { return null; }
                    @Override public boolean isSecure() { return false; }
                    @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
                    @Override public String getRealPath(String path) { return null; }
                    @Override public int getRemotePort() { return 0; }
                    @Override public String getLocalName() { return "localhost"; }
                    @Override public String getLocalAddr() { return "127.0.0.1"; }
                    @Override public int getLocalPort() { return 8080; }
                    @Override public jakarta.servlet.ServletContext getServletContext() { return null; }
                    @Override public jakarta.servlet.AsyncContext startAsync() throws IllegalStateException { return null; }
                    @Override public jakarta.servlet.AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) throws IllegalStateException { return null; }
                    @Override public boolean isAsyncStarted() { return false; }
                    @Override public boolean isAsyncSupported() { return false; }
                    @Override public jakarta.servlet.AsyncContext getAsyncContext() { return null; }
                    @Override public java.util.Map<String, String> getParameterMap() { return null; }
                    @Override public String getParameter(String name) { return null; }
                    @Override public java.util.Enumeration<String> getParameterNames() { return null; }
                    @Override public String[] getParameterValues(String name) { return null; }
                    @Override public String getProtocol() { return "HTTP/1.1"; }
                    @Override public String getScheme() { return "http"; }
                    @Override public String getServerName() { return "localhost"; }
                    @Override public int getServerPort() { return 8080; }
                    @Override public java.io.BufferedReader getReader() throws IOException { return null; }
                    @Override public String getRemoteAddr() { return "127.0.0.1"; }
                    @Override public String getRemoteHost() { return "localhost"; }
                    @Override public void setCharacterEncoding(String env) throws java.io.UnsupportedEncodingException {}
                    @Override public String getCharacterEncoding() { return "UTF-8"; }
                    @Override public int getContentLength() { return json.length(); }
                    @Override public long getContentLengthLong() { return json.length(); }
                    @Override public String getContentType() { return "application/json"; }
                };
        
        // Создание обертки
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(mockRequest);
        
        // Проверка
        String body = wrappedRequest.getBodyAsString();
        assertNotNull(body);
        assertEquals(json, body);
        
        // Проверка, что можно прочитать несколько раз
        String body2 = wrappedRequest.getBodyAsString();
        assertEquals(body, body2);
    }
    
    private static class MockServletInputStream extends jakarta.servlet.ServletInputStream {
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
            // Не используется
        }
    }
}


