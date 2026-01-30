package com.example.torrentutils.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 文件解压服务接口
 */
public interface FileExtractionService {

    /**
     * 处理文件夹：删除txt文件、解压rar文件
     *
     * @param folderPath 文件夹路径
     */
    void processFolder(String folderPath) throws IOException;

    /**
     * 删除txt文件
     *
     * @param txtFiles txt文件路径列表
     */
    void deleteTxtFiles(List<Path> txtFiles) throws IOException;

    /**
     * 解压rar文件
     *
     * @param rarFiles    rar文件路径列表
     * @param extractPath 解压目标路径
     */
    void extractRarFiles(List<Path> rarFiles, String extractPath) throws IOException;

    /**
     * 删除子文件夹及其内容
     *
     * @param parentDir 父目录路径
     */
    void deleteSubFolders(Path parentDir) throws IOException;
}
