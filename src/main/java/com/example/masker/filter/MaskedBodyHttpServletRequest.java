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
 * Обертка для HttpServletRequest, которая возвращает замаскированное тело запроса
 */
public class MaskedBodyHttpServletRequest extends HttpServletRequestWrapper {
    
    private final byte[] maskedBody;
    
    public MaskedBodyHttpServletRequest(HttpServletRequest request, String maskedBody) {
        super(request);
        this.maskedBody = maskedBody != null 
                ? maskedBody.getBytes(StandardCharsets.UTF_8) 
                : new byte[0];
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(maskedBody);
    }
    
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
    
    /**
     * Внутренний класс для чтения замаскированного тела
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
            throw new UnsupportedOperationException();
        }
    }
}

