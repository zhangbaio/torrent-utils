package com.example.torrentutils.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {

    /**
     * 上传单个文件
     *
     * @param file     上传的文件
     * @param dirPath  目标目录路径
     * @return 上传后的文件路径
     */
    String uploadFile(MultipartFile file, String dirPath) throws IOException;

    /**
     * 上传多个文件
     *
     * @param files    上传的文件列表
     * @param dirPath  目标目录路径
     * @return 上传后的文件路径列表
     */
    List<String> uploadMultipleFiles(MultipartFile[] files, String dirPath) throws IOException;

    /**
     * 创建目录（如果不存在）
     *
     * @param dirPath 目录路径
     */
    void createDirectoryIfNotExists(String dirPath) throws IOException;
}
