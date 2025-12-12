package com.example.masker.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Кастомная обертка для HttpServletRequest, которая кэширует тело запроса
 * для возможности многократного чтения
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
    
    private byte[] cachedBody;
    
    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(this.cachedBody);
    }
    
    @Override
    public BufferedReader getReader() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
    }
    
    /**
     * Получает тело запроса как строку
     */
    public String getBodyAsString() {
        return new String(this.cachedBody, StandardCharsets.UTF_8);
    }
    
    /**
     * Получает тело запроса как массив байтов
     */
    public byte[] getBodyAsBytes() {
        return this.cachedBody;
    }
    
    /**
     * Внутренний класс для ServletInputStream
     */
    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream buffer;
        
        public CachedBodyServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }
        
        @Override
        public int read() throws IOException {
            return buffer.read();
        }
        
        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }
        
        @Override
        public boolean isReady() {
            return true;
        }
        
        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException("ReadListener is not supported");
        }
    }
}


