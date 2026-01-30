package com.example.torrentutils.demos.web.webdav;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 1/16/26 2:12 PM
 */
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.DavResource;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.config.RequestConfig;

import java.io.*;
import java.util.*;

public class IPWebDAVClient {

    private Sardine sardine;
    private String baseUrl;

    /**
     * 构造函数 - 带超时设置
     */
    public IPWebDAVClient(String ip, int port, String basePath,
                          String username, String password,
                          int connectTimeout, int socketTimeout) {
        this.baseUrl = buildUrl(ip, port, basePath);

        // 创建带超时配置的 Sardine 实例
        HttpClientBuilder builder = HttpClientBuilder.create();
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .build();
        builder.setDefaultRequestConfig(config);

        this.sardine = SardineFactory.begin(username, password);
    }

    /**
     * 构造函数 - 使用默认超时
     */
    public IPWebDAVClient(String ip, int port, String basePath,
                          String username, String password) {
        this(ip, port, basePath, username, password, 30000, 30000);
    }

    private String buildUrl(String ip, int port, String basePath) {
        // 处理各种可能的URL格式
        StringBuilder url = new StringBuilder();
        url.append("http://").append(ip);

        if (port != 80) {
            url.append(":").append(port);
        }

        if (basePath != null && !basePath.isEmpty()) {
            if (!basePath.startsWith("/")) {
                url.append("/");
            }
            url.append(basePath);
        }

        if (!url.toString().endsWith("/")) {
            url.append("/");
        }

        return url.toString();
    }

    /**
     * 测试连接
     */
    public boolean testConnection() {
        try {
            sardine.list(baseUrl);
            return true;
        } catch (IOException e) {
            System.err.println("连接测试失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 列出目录内容
     */
    public List<WebDAVFile> listDirectory(String path) throws IOException {
        String url = baseUrl + (path.startsWith("/") ? path.substring(1) : path);
        List<DavResource> resources = sardine.list(url);
        List<WebDAVFile> result = new ArrayList<>();

        for (DavResource resource : resources) {
            // 跳过当前目录引用
            if (isSelfReference(resource, path)) {
                continue;
            }

            String name = extractFileName(resource.getPath(), path);
            WebDAVFile file = new WebDAVFile(
                    name,
                    resource.getPath(),
                    resource.isDirectory(),
                    resource.getContentLength(),
                    resource.getModified(),
                    resource.getContentType()
            );
            result.add(file);
        }

        return result;
    }

    /**
     * 非递归遍历文件
     */
    public List<WebDAVFile> listFilesNonRecursive(String path) throws IOException {
        List<WebDAVFile> allItems = listDirectory(path);
        List<WebDAVFile> files = new ArrayList<>();

        for (WebDAVFile item : allItems) {
            if (!item.isDirectory()) {
                files.add(item);
            }
        }

        return files;
    }

    /**
     * 下载文件
     */
    public void downloadFile(String remotePath, String localPath) throws IOException {
        String url = baseUrl + (remotePath.startsWith("/") ? remotePath.substring(1) : remotePath);
        System.out.println("下载URL: " + url);

        try (InputStream in = sardine.get(url);
             FileOutputStream out = new FileOutputStream(localPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            System.out.printf("下载完成: %s (大小: %s)%n",
                    localPath, formatSize(totalBytes));
        }
    }

    /**
     * 读取文件为字节数组
     */
    public byte[] readFileBytes(String remotePath) throws IOException {
        String url = baseUrl + (remotePath.startsWith("/") ? remotePath.substring(1) : remotePath);

        try (InputStream in = sardine.get(url);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            return out.toByteArray();
        }
    }

    /**
     * 读取文本文件
     */
    public String readTextFile(String remotePath, String charset) throws IOException {
        byte[] bytes = readFileBytes(remotePath);
        return new String(bytes, charset);
    }

    public String readTextFile(String remotePath) throws IOException {
        return readTextFile(remotePath, "UTF-8");
    }

    /**
     * 检查文件/目录是否存在
     */
    public boolean exists(String remotePath) throws IOException {
        String url = baseUrl + (remotePath.startsWith("/") ? remotePath.substring(1) : remotePath);
        return sardine.exists(url);
    }

    /**
     * 获取文件信息
     */
    public Optional<WebDAVFile> getFileInfo(String remotePath) throws IOException {
        String url = baseUrl + (remotePath.startsWith("/") ? remotePath.substring(1) : remotePath);
        List<DavResource> resources = sardine.list(url);

        if (resources.isEmpty()) {
            return Optional.empty();
        }

        DavResource resource = resources.get(0);
        String name = extractFileName(resource.getPath(), remotePath);

        WebDAVFile file = new WebDAVFile(
                name,
                resource.getPath(),
                resource.isDirectory(),
                resource.getContentLength(),
                resource.getModified(),
                resource.getContentType()
        );

        return Optional.of(file);
    }

    private boolean isSelfReference(DavResource resource, String currentPath) {
        String resourcePath = resource.getPath();
        String normalizedPath = currentPath.isEmpty() ? "" :
                (currentPath.startsWith("/") ? currentPath.substring(1) : currentPath);

        // 移除末尾的斜杠进行比较
        String cleanResourcePath = resourcePath.replaceAll("/+$", "");
        String cleanCurrentPath = normalizedPath.replaceAll("/+$", "");

        return cleanResourcePath.equals(cleanCurrentPath);
    }

    private String extractFileName(String fullPath, String basePath) {
        // 从完整路径中提取文件名
        String normalizedBase = basePath.isEmpty() ? "" :
                (basePath.startsWith("/") ? basePath.substring(1) : basePath);

        if (!normalizedBase.isEmpty() && !normalizedBase.endsWith("/")) {
            normalizedBase += "/";
        }

        if (fullPath.startsWith("/")) {
            fullPath = fullPath.substring(1);
        }

        if (fullPath.startsWith(normalizedBase)) {
            return fullPath.substring(normalizedBase.length());
        }

        // 如果无法匹配，返回最后一部分
        String[] parts = fullPath.split("/");
        return parts[parts.length - 1];
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * 文件信息类
     */
    public static class WebDAVFile {
        private String name;
        private String path;
        private boolean isDirectory;
        private long size;
        private Date modified;
        private String contentType;

        public WebDAVFile(String name, String path, boolean isDirectory,
                          long size, Date modified, String contentType) {
            this.name = name;
            this.path = path;
            this.isDirectory = isDirectory;
            this.size = size;
            this.modified = modified;
            this.contentType = contentType;
        }

        // getters
        public String getName() { return name; }
        public String getPath() { return path; }
        public boolean isDirectory() { return isDirectory; }
        public long getSize() { return size; }
        public Date getModified() { return modified; }
        public String getContentType() { return contentType; }


    }

    public static void main(String[] args) {
        // 示例：连接局域网内的WebDAV服务器
        String ip = "117.72.50.240";
        int port = 5244;
        String basePath = "webdav"; // WebDAV的根路径，如果没有则为空字符串
        String username = "admin";
        String password = "admin";

        try {
            // 创建客户端
            IPWebDAVClient client = new IPWebDAVClient(ip, port, basePath, username, password);

            // 测试连接
            System.out.println("=== 测试连接 ===");
            if (client.testConnection()) {
                System.out.println("连接成功!");
            } else {
                System.out.println("连接失败!");
                return;
            }

            // 列出根目录
            System.out.println("\n=== 根目录列表 ===");
            List<WebDAVFile> rootFiles = client.listFilesNonRecursive("");
            for (WebDAVFile file : rootFiles) {
                System.out.println(file);
            }

            // 列出子目录
            String subDir = "documents";
            System.out.println("\n=== 子目录: " + subDir + " ===");
            if (client.exists(subDir)) {
                List<WebDAVFile> subDirFiles = client.listFilesNonRecursive(subDir);
                for (WebDAVFile file : subDirFiles) {
                    System.out.println(file);
                }
            }

            // 下载文件示例
            String fileToDownload = "test.txt";
            if (client.exists(fileToDownload)) {
                System.out.println("\n=== 下载文件 ===");
                client.downloadFile(fileToDownload, "local-test.txt");
            }

            // 读取文本文件
            String textFile = "readme.txt";
            if (client.exists(textFile)) {
                System.out.println("\n=== 读取文本文件 ===");
                String content = client.readTextFile(textFile);
                System.out.println("文件内容(前200字符):");
                System.out.println(content.substring(0, Math.min(200, content.length())));
            }

            // 获取文件信息
            System.out.println("\n=== 文件信息 ===");
            Optional<WebDAVFile> fileInfo = client.getFileInfo(textFile);
            fileInfo.ifPresent(info -> {
                System.out.println("文件名: " + info.getName());
                System.out.println("大小: " + info.getSize() + " bytes");
                System.out.println("类型: " + info.getContentType());
                System.out.println("修改时间: " + info.getModified());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
