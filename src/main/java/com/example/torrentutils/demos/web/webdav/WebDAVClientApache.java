package com.example.torrentutils.demos.web.webdav;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 1/16/26 1:58 PM
 */
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import java.io.*;
import java.net.URI;

public class WebDAVClientApache {
    private CloseableHttpClient httpClient;
    private String baseUrl;

    public WebDAVClientApache(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.httpClient = createHttpClient(username, password);
    }

    private CloseableHttpClient createHttpClient(String username, String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        return HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
    }

    /**
     * 列出目录内容
     */
    public void listDirectory(String path) throws IOException, DavException {
        String url = baseUrl + path;
        HttpPropfind propfind = new HttpPropfind(url, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);

        try {
            org.apache.http.HttpResponse response = httpClient.execute(propfind);
            if (response.getStatusLine().getStatusCode() == 207) {
                MultiStatus multiStatus = propfind.getResponseBodyAsMultiStatus(response);
                MultiStatusResponse[] responses = multiStatus.getResponses();

                for (MultiStatusResponse msr : responses) {
                    String resourcePath = msr.getHref();
                    DavPropertySet properties = msr.getProperties(207);

                    DavProperty<?> resourceType = properties.get("resourcetype");
                    String type = "文件";
                    if (resourceType != null && resourceType.getValue().toString().contains("collection")) {
                        type = "目录";
                    }

                    DavProperty<?> displayName = properties.get("displayname");
                    DavProperty<?> contentLength = properties.get("getcontentlength");

                    System.out.printf("%s: %s (大小: %s)%n",
                            type,
                            displayName != null ? displayName.getValue() : resourcePath,
                            contentLength != null ? contentLength.getValue() : "未知"
                    );
                }
            }
        } finally {
            propfind.releaseConnection();
        }
    }

    /**
     * 下载文件
     */
    public void downloadFile(String remotePath, String localPath) throws IOException {
        String url = baseUrl + remotePath;
        HttpGet httpGet = new HttpGet(url);

        try (InputStream in = httpClient.execute(httpGet).getEntity().getContent();
             FileOutputStream out = new FileOutputStream(localPath)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            System.out.println("文件下载完成: " + localPath);
        } finally {
            httpGet.releaseConnection();
        }
    }

    /**
     * 获取文件内容为字符串
     */
    public String getFileContent(String remotePath) throws IOException {
        String url = baseUrl + remotePath;
        HttpGet httpGet = new HttpGet(url);

        try (InputStream in = httpClient.execute(httpGet).getEntity().getContent();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            return content.toString();
        } finally {
            httpGet.releaseConnection();
        }
    }

    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    public static void main(String[] args) {
        WebDAVClientApache client = null;
        try {
            // 示例：Nextcloud/ownCloud WebDAV
            String baseUrl = "http://117.72.50.240:5244/dav/files/username/";
            String username = "admin";
            String password = "admin";

            client = new WebDAVClientApache(baseUrl, username, password);

            // 列出根目录
            System.out.println("=== 列出目录 ===");
            client.listDirectory("");
//
//            // 下载文件
//            System.out.println("\n=== 下载文件 ===");
//            client.downloadFile("/path/to/file.txt", "local-file.txt");
//
//            // 读取文件内容
//            System.out.println("\n=== 读取文件内容 ===");
//            String content = client.getFileContent("/path/to/file.txt");
//            System.out.println(content.substring(0, Math.min(200, content.length())));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
