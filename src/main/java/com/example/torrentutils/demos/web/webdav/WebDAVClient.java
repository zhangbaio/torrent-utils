package com.example.torrentutils.demos.web.webdav;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 1/16/26 2:18 PM
 */
/*
 * Maven 依赖配置 (pom.xml) - 适用于 Java 8:
 *
 * <dependencies>
 *     <!-- Sardine WebDAV 客户端 (Java 8 兼容版本) -->
 *     <dependency>
 *         <groupId>com.github.lookfirst</groupId>
 *         <artifactId>sardine</artifactId>
 *         <version>5.9</version>
 *     </dependency>
 * </dependencies>
 *
 * Gradle 配置 (build.gradle) - 适用于 Java 8:
 *
 * dependencies {
 *     implementation 'com.github.lookfirst:sardine:5.9'
 * }
 *
 * 注意：Java 8 内置了 JAXB，所以只需要 Sardine 依赖即可
 */

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.DavResource;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class WebDAVClient {

    public static void main(String[] args) {
        // WebDAV 服务器配置
        String ip = "117.72.50.240";
        int port = 5244;
        String username = "admin";
        String password = "admin";

        // 常见的 WebDAV 路径，按顺序尝试
        String[] possiblePaths = {
                "/",                    // 根路径
                "/webdav/",            // 常见路径
                "/dav/",               // 另一个常见路径
                "/remote.php/webdav/"  // Nextcloud/ownCloud 路径
        };

        try {
            // 创建 Sardine 客户端
            Sardine sardine = SardineFactory.begin(username, password);

            // 如果没有用户名密码，创建无认证客户端
            if (username == null || username.isEmpty()) {
                sardine = SardineFactory.begin();
            }

            // 测试连接并找到正确的路径
            String webdavUrl = testConnection(sardine, ip, port, possiblePaths);

            if (webdavUrl == null) {
                System.err.println("无法连接到 WebDAV 服务器，请检查:");
                System.err.println("1. IP 地址和端口是否正确");
                System.err.println("2. WebDAV 服务是否已启动");
                System.err.println("3. 防火墙是否允许访问");
                System.err.println("4. 用户名和密码是否正确");
                return;
            }

            System.out.println("成功连接到: " + webdavUrl);
            System.out.println();

            // 示例1: 列出目录中的文件
            System.out.println("=== 列出目录文件 ===");
            listFiles(sardine, webdavUrl);

            // 示例2: 读取文件内容（如果存在）
            // String filePath = webdavUrl + "example.txt";
            // System.out.println("\n=== 读取文件内容 ===");
            // readFile(sardine, filePath);

        } catch (Exception e) {
            System.err.println("WebDAV 操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试连接并找到正确的 WebDAV 路径
     */
    public static String testConnection(Sardine sardine, String ip, int port, String[] paths) {
        for (String path : paths) {
            String url = "http://" + ip + ":" + port + path;
            System.out.println("尝试连接: " + url);

            try {
                // 尝试检查路径是否存在
                if (sardine.exists(url)) {
                    System.out.println("✓ 连接成功!");
                    return url;
                }
            } catch (Exception e) {
                System.out.println("✗ 失败: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * 列出目录中的文件
     */
    public static void listFiles(Sardine sardine, String url) throws Exception {
        List<DavResource> resources = sardine.list(url);

        for (DavResource res : resources) {
            System.out.println("名称: " + res.getName());
            System.out.println("  路径: " + res.getPath());
            System.out.println("  是否目录: " + res.isDirectory());
            System.out.println("  大小: " + res.getContentLength() + " 字节");
            System.out.println("  修改时间: " + res.getModified());
            System.out.println();
        }
    }

    /**
     * 读取文本文件内容
     */
    public static void readFile(Sardine sardine, String fileUrl) throws Exception {
        // 获取文件输入流
        InputStream is = sardine.get(fileUrl);

        // 读取文件内容
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;

        System.out.println("文件内容:");
        System.out.println("---");
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println("---");

        reader.close();
        is.close();
    }

    /**
     * 下载文件到本地
     */
    public static void downloadFile(Sardine sardine, String remoteUrl, String localPath) throws Exception {
        InputStream is = sardine.get(remoteUrl);

        // 将输入流写入本地文件
        java.io.FileOutputStream fos = new java.io.FileOutputStream(localPath);
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }

        fos.close();
        is.close();

        System.out.println("文件已下载到: " + localPath);
    }

    /**
     * 检查文件/目录是否存在
     */
    public static boolean exists(Sardine sardine, String url) throws Exception {
        return sardine.exists(url);
    }

    /**
     * 创建目录
     */
    public static void createDirectory(Sardine sardine, String dirUrl) throws Exception {
        sardine.createDirectory(dirUrl);
        System.out.println("目录已创建: " + dirUrl);
    }

    /**
     * 上传文件
     */
    public static void uploadFile(Sardine sardine, String localPath, String remoteUrl) throws Exception {
        java.io.FileInputStream fis = new java.io.FileInputStream(localPath);
        sardine.put(remoteUrl, fis, "application/octet-stream");
        fis.close();
        System.out.println("文件已上传: " + remoteUrl);
    }

    /**
     * 删除文件或目录
     */
    public static void delete(Sardine sardine, String url) throws Exception {
        sardine.delete(url);
        System.out.println("已删除: " + url);
    }
}