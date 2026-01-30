package com.example.torrentutils.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件清理结果
 */
public class CleanupResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 清理的文件数量
     */
    private int fileCount;

    /**
     * 清理的目录数量
     */
    private int dirCount;

    /**
     * 释放的磁盘空间（字节）
     */
    private long freedSpace;

    /**
     * 清理的文件列表
     */
    private List<String> cleanedFiles = new ArrayList<>();

    /**
     * 清理失败的文件列表
     */
    private List<String> failedFiles = new ArrayList<>();

    /**
     * 错误信息
     */
    private String errorMessage;

    public CleanupResult() {
        this.success = true;
    }

    public static CleanupResult success() {
        return new CleanupResult();
    }

    public static CleanupResult error(String message) {
        CleanupResult result = new CleanupResult();
        result.setSuccess(false);
        result.setErrorMessage(message);
        return result;
    }

    public void addCleanedFile(String path, long size) {
        this.cleanedFiles.add(path);
        this.fileCount++;
        this.freedSpace += size;
    }

    public void addCleanedDir(String path) {
        this.cleanedFiles.add(path + " (目录)");
        this.dirCount++;
    }

    public void addFailedFile(String path) {
        this.failedFiles.add(path);
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public int getDirCount() {
        return dirCount;
    }

    public void setDirCount(int dirCount) {
        this.dirCount = dirCount;
    }

    public long getFreedSpace() {
        return freedSpace;
    }

    public void setFreedSpace(long freedSpace) {
        this.freedSpace = freedSpace;
    }

    public List<String> getCleanedFiles() {
        return cleanedFiles;
    }

    public void setCleanedFiles(List<String> cleanedFiles) {
        this.cleanedFiles = cleanedFiles;
    }

    public List<String> getFailedFiles() {
        return failedFiles;
    }

    public void setFailedFiles(List<String> failedFiles) {
        this.failedFiles = failedFiles;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 获取格式化的磁盘空间
     */
    public String getFormattedFreedSpace() {
        if (freedSpace < 1024) {
            return freedSpace + " B";
        } else if (freedSpace < 1024 * 1024) {
            return String.format("%.2f KB", freedSpace / 1024.0);
        } else if (freedSpace < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", freedSpace / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", freedSpace / (1024.0 * 1024 * 1024));
        }
    }
}
