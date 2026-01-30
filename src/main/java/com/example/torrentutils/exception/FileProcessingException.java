package com.example.torrentutils.exception;

/**
 * 文件处理异常
 */
public class FileProcessingException extends RuntimeException {

    private String errorCode;

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileProcessingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
