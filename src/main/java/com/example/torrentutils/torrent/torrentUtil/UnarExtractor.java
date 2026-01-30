package com.example.torrentutils.torrent.torrentUtil;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 6/30/25 12:42 PM
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class UnarExtractor {

    public static void extractWithUnar(File archive, File outputDir) throws IOException, InterruptedException {
        // 检查文件是否存在
        if (!archive.exists()) {
            throw new IOException("压缩文件不存在: " + archive.getAbsolutePath());
        }

        // 确保输出目录存在
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // 构建命令
        ProcessBuilder pb = new ProcessBuilder(
                "/opt/homebrew/bin/unar",
                "-o", outputDir.getAbsolutePath(),  // 输出目录
                "-f",  // 强制覆盖已存在文件
                archive.getAbsolutePath()  // 压缩文件路径
        );

        // 重定向错误流到标准输出
        pb.redirectErrorStream(true);

        // 执行命令
        Process process = pb.start();

        // 读取输出
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);  // 打印解压过程信息
            }
        }

        // 等待命令完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("解压失败，退出码: " + exitCode);
        }
    }

    public static void main(String[] args) {
        try {
            File archive = new File("/Users/zhangbiao/Documents/视频/video/原始种子文件/2024/Japan日本欧美1Pondo10musumeCaribbeancomPacopacomama高清超清蓝光种子2024年02月.rar");
            File outputDir = new File("/Users/zhangbiao/Documents/视频/video/原始种子文件/2024");

            extractWithUnar(archive, outputDir);
            System.out.println("解压成功!");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}