package com.example.torrentutils.torrent.torrentUtil;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileProcessor {

    public static void main(String[] args) {
        String folderPath = "/Users/zhangbiao/Documents/视频/video/2021"; // 替换为实际路径
        processFolder(folderPath);
        //TorrentToMagnetConverter.convertTorrentsToMagnets(folderPath);

    }

    public static void processFolder(String folderPath) {
        try {
            Path folder = Paths.get(folderPath);

            if (!Files.exists(folder) || !Files.isDirectory(folder)) {
                System.out.println("指定路径不存在或不是文件夹: " + folderPath);
                return;
            }

            // 获取文件夹中的所有文件
            List<Path> txtFiles = new ArrayList<>();
            List<Path> rarFiles = new ArrayList<>();

            Files.walk(folder, 1) // 只处理当前层级，不递归子文件夹
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString().toLowerCase();
                        if (fileName.endsWith(".torrent")) {
                            txtFiles.add(file);
                        } else if (fileName.endsWith(".rar")) {
                            rarFiles.add(file);
                        }
                    });

            // 删除txt文件
            deleteTxtFiles(txtFiles);

            // 解压rar文件
            extractRarFiles(rarFiles, folderPath);

        } catch (IOException e) {
            System.err.println("处理文件夹时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteTxtFiles(List<Path> txtFiles) {
        System.out.println("开始删除txt文件...");
        for (Path txtFile : txtFiles) {
            try {
                Files.delete(txtFile);
                System.out.println("已删除: " + txtFile.getFileName());
            } catch (IOException e) {
                System.err.println("删除文件失败: " + txtFile.getFileName() + " - " + e.getMessage());
            }
        }
        System.out.println("txt文件删除完成，共删除 " + txtFiles.size() + " 个文件。");
    }


    private static void extractRarFiles(List<Path> rarFiles, String extractPath) {
        System.out.println("开始解压rar文件...");

        for (Path rarFile : rarFiles) {
            try {
                // 创建解压目录
                String rarFileName = rarFile.getFileName().toString();
                String extractFolderName = rarFileName.substring(0, rarFileName.lastIndexOf('.'));
                Path extractFolder = Paths.get(extractPath, extractFolderName);
                Files.createDirectories(extractFolder);

                // 使用junrar库解压
                Archive archive = new Archive(rarFile.toFile());

                FileHeader fileHeader = archive.nextFileHeader();
                while (fileHeader != null) {
                    File extractedFile = new File(extractFolder.toFile(), fileHeader.getFileName());

                    // 确保父目录存在
                    extractedFile.getParentFile().mkdirs();

                    if (!fileHeader.isDirectory()) {
                        try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                            archive.extractFile(fileHeader, fos);
                        }
                    }

                    fileHeader = archive.nextFileHeader();
                }

                archive.close();
                System.out.println("解压成功: " + rarFile.getFileName());

            } catch (Exception e) {
                System.err.println("解压文件时发生错误: " + rarFile.getFileName() + " - " + e.getMessage());
            }
        }
        System.out.println("rar文件解压完成，共处理 " + rarFiles.size() + " 个文件。");
    }

    private static boolean extractWithWinRAR(String rarPath, String extractPath) {
        try {
            // WinRAR命令：winrar x -o+ "rar文件路径" "解压路径"
            ProcessBuilder pb = new ProcessBuilder("winrar", "x", "-o+", rarPath, extractPath);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean extractWith7Zip(String rarPath, String extractPath) {
        try {
            // 7-Zip命令：7z x "rar文件路径" -o"解压路径"
            ProcessBuilder pb = new ProcessBuilder("7z", "x", rarPath, "-o" + extractPath);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}