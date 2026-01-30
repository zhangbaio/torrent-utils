package com.example.torrentutils.service.impl;

import com.example.torrentutils.service.FileExtractionService;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件解压服务实现
 */
@Service
public class FileExtractionServiceImpl implements FileExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(FileExtractionServiceImpl.class);

    @Override
    public void processFolder(String folderPath) throws IOException {
        Path folder = Paths.get(folderPath);

        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            logger.warn("指定路径不存在或不是文件夹: {}", folderPath);
            return;
        }

        List<Path> txtFiles = new ArrayList<>();
        List<Path> rarFiles = new ArrayList<>();

        // 遍历文件夹，收集txt和rar文件
        Files.walk(folder, 1)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    String fileName = file.getFileName().toString().toLowerCase();
                    if (fileName.endsWith(".txt")) {
                        txtFiles.add(file);
                    } else if (fileName.endsWith(".rar")) {
                        rarFiles.add(file);
                    }
                });

        // 删除txt文件
        deleteTxtFiles(txtFiles);

        // 解压rar文件
        extractRarFiles(rarFiles, folderPath);
    }

    @Override
    public void deleteTxtFiles(List<Path> txtFiles) throws IOException {
        logger.info("开始删除txt文件...");
        for (Path txtFile : txtFiles) {
            try {
                Files.delete(txtFile);
                logger.info("已删除: {}", txtFile.getFileName());
            } catch (IOException e) {
                logger.error("删除文件失败: {} - {}", txtFile.getFileName(), e.getMessage());
                throw e;
            }
        }
        logger.info("txt文件删除完成，共删除 {} 个文件", txtFiles.size());
    }

    @Override
    public void extractRarFiles(List<Path> rarFiles, String extractPath) {
        logger.info("开始解压rar文件...");

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
                    java.io.File extractedFile = new java.io.File(extractFolder.toFile(), fileHeader.getFileName());

                    // 确保父目录存在
                    if (extractedFile.getParentFile() != null) {
                        extractedFile.getParentFile().mkdirs();
                    }

                    if (!fileHeader.isDirectory()) {
                        try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                            archive.extractFile(fileHeader, fos);
                        }
                    }

                    fileHeader = archive.nextFileHeader();
                }

                archive.close();
                logger.info("解压成功: {}", rarFile.getFileName());

            } catch (Exception e) {
                logger.error("解压文件时发生错误: {} - {}", rarFile.getFileName(), e.getMessage());
            }
        }
        logger.info("rar文件解压完成，共处理 {} 个文件", rarFiles.size());
    }

    @Override
    public void deleteSubFolders(Path parentDir) throws IOException {
        if (!Files.exists(parentDir)) {
            return;
        }

        Files.walk(parentDir, 1)  // 只遍历直接子项
                .filter(Files::isDirectory)  // 只处理目录
                .filter(path -> !path.equals(parentDir))  // 排除父目录本身
                .forEach(path -> {
                    try {
                        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                    throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                    throws IOException {
                                Files.delete(dir);
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (IOException e) {
                        logger.error("删除子文件夹失败: {} - {}", path, e.getMessage());
                    }
                });
    }
}
