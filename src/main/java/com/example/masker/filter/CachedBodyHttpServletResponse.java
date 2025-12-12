package com.example.masker.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Кастомная обертка для HttpServletResponse, которая кэширует тело ответа
 * для возможности чтения после записи
 */
public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {
    
    private final ByteArrayOutputStream cachedContent;
    private ServletOutputStream outputStream;
    private PrintWriter writer;
    
    public CachedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
        this.cachedContent = new ByteArrayOutputStream();
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.outputStream == null) {
            this.outputStream = new CachedBodyServletOutputStream(this.cachedContent);
        }
        return this.outputStream;
    }
    
    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.writer == null) {
            this.writer = new PrintWriter(
                new java.io.OutputStreamWriter(
                    new ServletOutputStreamAdapter(getOutputStream()), 
                    StandardCharsets.UTF_8
                ), 
                true
            );
        }
        return this.writer;
    }
    
    /**
     * Получает тело ответа как строку
     */
    public String getBodyAsString() {
        return new String(this.cachedContent.toByteArray(), StandardCharsets.UTF_8);
    }
    
    /**
     * Получает тело ответа как массив байтов
     */
    public byte[] getBodyAsBytes() {
        return this.cachedContent.toByteArray();
    }
    
    /**
     * Копирует кэшированное тело ответа в оригинальный response
     */
    public void copyBodyToResponse() throws IOException {
        if (this.cachedContent.size() > 0) {
            HttpServletResponse originalResponse = (HttpServletResponse) getResponse();
            originalResponse.getOutputStream().write(this.cachedContent.toByteArray());
            originalResponse.getOutputStream().flush();
        }
    }
    
    /**
     * Внутренний класс для ServletOutputStream
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
        public void write(byte[] b) throws IOException {
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
            throw new UnsupportedOperationException("WriteListener is not supported");
        }
    }
    
    /**
     * Адаптер для преобразования ServletOutputStream в OutputStream
     */
    private static class ServletOutputStreamAdapter extends java.io.OutputStream {
        private final ServletOutputStream servletOutputStream;
        
        public ServletOutputStreamAdapter(ServletOutputStream servletOutputStream) {
            this.servletOutputStream = servletOutputStream;
        }
        
        @Override
        public void write(int b) throws IOException {
            servletOutputStream.write(b);
        }
        
        @Override
        public void write(byte[] b) throws IOException {
            servletOutputStream.write(b);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            servletOutputStream.write(b, off, len);
        }
        
        @Override
        public void flush() throws IOException {
            servletOutputStream.flush();
        }
        
        @Override
        public void close() throws IOException {
            servletOutputStream.close();
        }
    }
}

