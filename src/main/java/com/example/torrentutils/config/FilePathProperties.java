package com.example.torrentutils.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件路径配置类
 * 统一管理所有文件路径配置
 */
@Component
@ConfigurationProperties(prefix = "app")
public class FilePathProperties {

    /**
     * 基础上传目录
     */
    private String uploadDir = "uploads/";

    /**
     * 种子文件上传目录
     */
    private String uploadTorrentDir = "uploads/torrent/";

    /**
     * 原始压缩文件目录
     */
    private String originalFileDir = "压缩种子文件/";

    /**
     * 磁力链接汇总目录
     */
    private String allFileDir = "磁力链接汇总/";

    /**
     * 种子分类目录
     */
    private String classifyFileDir = "种子分类/";

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getUploadTorrentDir() {
        return uploadTorrentDir;
    }

    public void setUploadTorrentDir(String uploadTorrentDir) {
        this.uploadTorrentDir = uploadTorrentDir;
    }

    public String getOriginalFileDir() {
        return originalFileDir;
    }

    public void setOriginalFileDir(String originalFileDir) {
        this.originalFileDir = originalFileDir;
    }

    public String getAllFileDir() {
        return allFileDir;
    }

    public void setAllFileDir(String allFileDir) {
        this.allFileDir = allFileDir;
    }

    public String getClassifyFileDir() {
        return classifyFileDir;
    }

    public void setClassifyFileDir(String classifyFileDir) {
        this.classifyFileDir = classifyFileDir;
    }

    /**
     * 获取完整的基础目录路径
     */
    public String resolveBasePath() {
        String baseDir = System.getProperty("user.dir");
        return baseDir;
    }

    /**
     * 解析相对路径为绝对路径
     */
    public String resolvePath(String relativePath) {
        if (relativePath.startsWith("./")) {
            String baseDir = System.getProperty("user.dir");
            return java.nio.file.Paths.get(baseDir, relativePath.substring(2)).toString();
        }
        if (!java.nio.file.Paths.get(relativePath).isAbsolute()) {
            String baseDir = System.getProperty("user.dir");
            return java.nio.file.Paths.get(baseDir, relativePath).toString();
        }
        return relativePath;
    }
}
