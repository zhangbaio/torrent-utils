package com.example.torrentutils.demos.web.webdav;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 1/16/26 2:05 PM
 */
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.DavResource;

import java.io.*;
import java.util.List;

public class WebDAVClientSardine {
    private Sardine sardine;
    private String baseUrl;

    public WebDAVClientSardine(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.sardine = SardineFactory.begin(username, password);
    }

    /**
     * 列出目录内容
     */
    public void listDirectory(String path) throws IOException {
        String url = baseUrl + path;
        List<DavResource> resources = sardine.list(url);

        System.out.println("目录: " + path);
        for (DavResource resource : resources) {
            // 跳过当前目录自身
            if (resource.getPath().equals(path) || resource.getPath().equals(path + "/")) {
                continue;
            }

            String type = resource.isDirectory() ? "目录" : "文件";
            String size = resource.isDirectory() ? "-" : formatSize(resource.getContentLength());

            System.out.printf("%-6s %-40s %-10s %s%n",
                    type,
                    resource.getName(),
                    size,
                    resource.getModified()
            );
        }
    }

    /**
     * 下载文件
     */
    public void downloadFile(String remotePath, String localPath) throws IOException {
        String url = baseUrl + remotePath;
        InputStream in = sardine.get(url);

        try (FileOutputStream out = new FileOutputStream(localPath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        System.out.println("下载完成: " + localPath);
    }

    /**
     * 读取文件内容为字符串
     */
    public String readFileAsString(String remotePath) throws IOException {
        String url = baseUrl + remotePath;
        InputStream in = sardine.get(url);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean exists(String remotePath) throws IOException {
        String url = baseUrl + remotePath;
        return sardine.exists(url);
    }

    /**
     * 获取文件信息
     */
    public DavResource getFileInfo(String remotePath) throws IOException {
        String url = baseUrl + remotePath;
        List<DavResource> resources = sardine.list(url);
        return resources.isEmpty() ? null : resources.get(0);
    }

    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }

    public static void main(String[] args) {
        try {
            // 不同WebDAV服务的URL格式示例：
            // Nextcloud: https://nextcloud.example.com/remote.php/dav/files/USERNAME/
            // ownCloud: https://owncloud.example.com/remote.php/webdav/
            // 其他WebDAV: https://webdav.example.com/

            String baseUrl = "http://117.72.50.240:5244/webdav/";
            String username = "admin";
            String password = "admin";

            WebDAVClientSardine client = new WebDAVClientSardine(baseUrl, username, password);

            // 1. 列出目录
            System.out.println("=== 目录列表 ===");
            client.listDirectory("/");

//            // 2. 检查文件是否存在
//            String filePath = "/documents/example.txt";
//            System.out.println("\n=== 检查文件 ===");
//            System.out.println("文件是否存在: " + client.exists(filePath));
//
//            // 3. 获取文件信息
//            DavResource info = client.getFileInfo(filePath);
//            if (info != null) {
//                System.out.println("文件名: " + info.getName());
//                System.out.println("大小: " + info.getContentLength() + " bytes");
//                System.out.println("修改时间: " + info.getModified());
//            }
//
//            // 4. 读取文件内容
//            System.out.println("\n=== 文件内容 ===");
//            String content = client.readFileAsString(filePath);
//            System.out.println(content.substring(0, Math.min(500, content.length())));
//
//            // 5. 下载文件
//            System.out.println("\n=== 下载文件 ===");
//            client.downloadFile(filePath, "downloaded-file.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
