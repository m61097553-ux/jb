package com.example.masker.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Обертка для HttpServletResponse, которая кэширует тело ответа для маскировки
 */
public class MaskedBodyHttpServletResponse extends HttpServletResponseWrapper {
    
    private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;
    private PrintWriter writer;
    
    public MaskedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called on this response.");
        }
        
        if (outputStream == null) {
            outputStream = new CachedBodyServletOutputStream(cachedBody);
        }
        
        return outputStream;
    }
    
    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called on this response.");
        }
        
        if (writer == null) {
            writer = new PrintWriter(new java.io.OutputStreamWriter(cachedBody, StandardCharsets.UTF_8));
        }
        
        return writer;
    }
    
    @Override
    public void flushBuffer() throws IOException {
        if (outputStream != null) {
            outputStream.flush();
        }
        if (writer != null) {
            writer.flush();
        }
        // Сбрасываем флаг, чтобы избежать повторных flush
        super.flushBuffer();
    }
    
    /**
     * Получить закэшированное тело ответа как строку
     */
    public String getCachedBodyAsString() {
        flushBuffer();
        return cachedBody.toString(StandardCharsets.UTF_8);
    }
    
    /**
     * Получить закэшированное тело ответа как байты
     */
    public byte[] getCachedBodyAsBytes() {
        flushBuffer();
        return cachedBody.toByteArray();
    }
    
    /**
     * Внутренний класс для кэширования тела ответа
     */
    private static class CachedBodyServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream buffer;
        
        public CachedBodyServletOutputStream(ByteArrayOutputStream buffer) {
            this.buffer = buffer;
        }
        
        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            buffer.write(b, off, len);
        }
        
        @Override
        public boolean isReady() {
            return true;
        }
        
        @Override
        public void setWriteListener(WriteListener listener) {
            throw new UnsupportedOperationException();
        }
    }
}

