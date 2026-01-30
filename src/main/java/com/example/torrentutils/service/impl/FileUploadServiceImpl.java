package com.example.torrentutils.service.impl;

import com.example.torrentutils.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Override
    public String uploadFile(MultipartFile file, String dirPath) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        // 创建目录
        createDirectoryIfNotExists(dirPath);

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "unnamed";
        }

        // 使用原始文件名保存（与原逻辑保持一致）
        Path targetPath = Paths.get(dirPath, originalFilename);

        // 保存文件
        Files.write(targetPath, file.getBytes());

        return targetPath.toString();
    }

    @Override
    public List<String> uploadMultipleFiles(MultipartFile[] files, String dirPath) throws IOException {
        List<String> uploadedPaths = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            String path = uploadFile(file, dirPath);
            uploadedPaths.add(path);
        }

        return uploadedPaths;
    }

    @Override
    public void createDirectoryIfNotExists(String dirPath) throws IOException {
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }
}
