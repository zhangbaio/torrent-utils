package com.example.torrentutils.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件清理配置类
 * 从 application.yml 读取清理任务配置
 */
@Component
@ConfigurationProperties(prefix = "app.cleanup")
public class CleanupProperties {

    /**
     * 是否启用清理任务
     */
    private boolean enabled = true;

    /**
     * 定时任务执行时间（cron 表达式）
     * 默认：每天凌晨 2 点执行
     */
    private String cron = "0 0 2 * * ?";

    /**
     * 文件保留多少小时后清理（默认 24 小时）
     */
    private int delayHours = 24;

    /**
     * 是否清理中间文件（torrent、压缩包）
     */
    private boolean cleanupIntermediateFiles = true;

    /**
     * 是否清理整个工作目录（包括磁力链接汇总文件）
     */
    private boolean cleanupWorkDir = false;

    /**
     * 是否清理磁力链接汇总文件
     */
    private boolean cleanupMagnetFiles = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getDelayHours() {
        return delayHours;
    }

    public void setDelayHours(int delayHours) {
        this.delayHours = delayHours;
    }

    public boolean isCleanupIntermediateFiles() {
        return cleanupIntermediateFiles;
    }

    public void setCleanupIntermediateFiles(boolean cleanupIntermediateFiles) {
        this.cleanupIntermediateFiles = cleanupIntermediateFiles;
    }

    public boolean isCleanupWorkDir() {
        return cleanupWorkDir;
    }

    public void setCleanupWorkDir(boolean cleanupWorkDir) {
        this.cleanupWorkDir = cleanupWorkDir;
    }

    public boolean isCleanupMagnetFiles() {
        return cleanupMagnetFiles;
    }

    public void setCleanupMagnetFiles(boolean cleanupMagnetFiles) {
        this.cleanupMagnetFiles = cleanupMagnetFiles;
    }
}
