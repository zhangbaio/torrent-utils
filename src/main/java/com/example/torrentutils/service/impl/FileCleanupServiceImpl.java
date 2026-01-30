package com.example.torrentutils.service.impl;

import com.example.torrentutils.config.CleanupProperties;
import com.example.torrentutils.config.FilePathProperties;
import com.example.torrentutils.model.CleanupResult;
import com.example.torrentutils.service.FileCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件清理服务实现
 */
@Service
public class FileCleanupServiceImpl implements FileCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(FileCleanupServiceImpl.class);

    @Autowired
    private CleanupProperties cleanupProperties;

    @Autowired
    private FilePathProperties filePathProperties;

    @Override
    public CleanupResult cleanupExpiredFiles() {
        return cleanupExpiredFiles(cleanupProperties.getDelayHours());
    }

    @Override
    public CleanupResult cleanupExpiredFiles(int delayHours) {
        CleanupResult result = CleanupResult.success();

        String baseDir = filePathProperties.resolveBasePath();
        String originalFileDir = filePathProperties.resolvePath(filePathProperties.getOriginalFileDir());

        File originalDir = new File(originalFileDir);
        if (!originalDir.exists() || !originalDir.isDirectory()) {
            logger.info("原始文件目录不存在: {}", originalFileDir);
            return result;
        }

        // 获取所有工作目录（UUID 目录）
        File[] workDirs = originalDir.listFiles(File::isDirectory);
        if (workDirs == null || workDirs.length == 0) {
            logger.info("没有找到需要清理的工作目录");
            return result;
        }

        for (File workDir : workDirs) {
            CleanupResult dirResult = cleanupWorkDir(workDir.getAbsolutePath(), delayHours);
            mergeResult(result, dirResult);
        }

        logger.info("清理完成: 删除 {} 个文件, {} 个目录, 释放 {}",
                result.getFileCount(), result.getDirCount(), result.getFormattedFreedSpace());

        return result;
    }

    @Override
    public CleanupResult cleanupWorkDir(String workDir) {
        return cleanupWorkDir(workDir, cleanupProperties.getDelayHours());
    }

    @Override
    public CleanupResult manualCleanup(Integer delayHours) {
        int hours = delayHours != null ? delayHours : cleanupProperties.getDelayHours();
        logger.info("手动触发清理任务，延迟时间: {} 小时", hours);
        return cleanupExpiredFiles(hours);
    }

    /**
     * 清理指定工作目录
     *
     * @param workDirPath 工作目录路径
     * @param delayHours  延迟小时数
     * @return 清理结果
     */
    private CleanupResult cleanupWorkDir(String workDirPath, int delayHours) {
        CleanupResult result = CleanupResult.success();

        File workDir = new File(workDirPath);
        if (!workDir.exists() || !workDir.isDirectory()) {
            logger.warn("工作目录不存在: {}", workDirPath);
            return result;
        }

        Instant expireTime = Instant.now().minus(delayHours, ChronoUnit.HOURS);
        List<File> dirsToDelete = new ArrayList<>();

        // 清理中间文件
        if (cleanupProperties.isCleanupIntermediateFiles()) {
            cleanupIntermediateFiles(workDir, expireTime, result, dirsToDelete);
        }

        // 清理磁力链接汇总文件
        if (cleanupProperties.isCleanupMagnetFiles()) {
            cleanupMagnetFiles(workDir, expireTime, result, dirsToDelete);
        }

        // 清理空目录和过期的工作目录
        if (cleanupProperties.isCleanupWorkDir()) {
            cleanupEmptyWorkDir(workDir, expireTime, result, dirsToDelete);
        }

        // 删除标记的目录
        for (File dir : dirsToDelete) {
            if (deleteDirectoryRecursively(dir)) {
                result.addCleanedDir(dir.getAbsolutePath());
            } else {
                result.addFailedFile(dir.getAbsolutePath());
            }
        }

        return result;
    }

    /**
     * 清理中间文件（压缩包、torrent 文件）
     */
    private void cleanupIntermediateFiles(File workDir, Instant expireTime,
                                          CleanupResult result, List<File> dirsToDelete) {
        // 清理原始压缩包
        cleanupFilesByExtension(workDir, expireTime, result,
                ".zip", ".rar", ".7z", ".tar", ".gz");

        // 清理解压出的 torrent 文件
        File classifyDir = new File(workDir, filePathProperties.getClassifyFileDir());
        if (classifyDir.exists() && classifyDir.isDirectory()) {
            cleanupFilesByExtension(classifyDir, expireTime, result, ".torrent");
            markEmptyDirectories(classifyDir, dirsToDelete);
        }
    }

    /**
     * 清理磁力链接汇总文件
     */
    private void cleanupMagnetFiles(File workDir, Instant expireTime,
                                     CleanupResult result, List<File> dirsToDelete) {
        File magnetDir = new File(workDir, filePathProperties.getAllFileDir());
        if (magnetDir.exists() && magnetDir.isDirectory()) {
            cleanupFilesByExtension(magnetDir, expireTime, result, ".txt");
            if (isDirectoryEmpty(magnetDir)) {
                dirsToDelete.add(magnetDir);
            }
        }
    }

    /**
     * 标记空目录
     */
    private void markEmptyDirectories(File dir, List<File> dirsToDelete) {
        File[] subDirs = dir.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                if (isDirectoryEmpty(subDir)) {
                    dirsToDelete.add(subDir);
                }
            }
        }
        if (isDirectoryEmpty(dir)) {
            dirsToDelete.add(dir);
        }
    }

    /**
     * 清理空的工作目录
     */
    private void cleanupEmptyWorkDir(File workDir, Instant expireTime,
                                      CleanupResult result, List<File> dirsToDelete) {
        // 检查工作目录本身是否过期
        try {
            FileTime lastModified = Files.getLastModifiedTime(workDir.toPath());
            if (lastModified.toInstant().isBefore(expireTime) && isDirectoryEmpty(workDir)) {
                dirsToDelete.add(workDir);
            }
        } catch (IOException e) {
            logger.warn("无法读取目录修改时间: {}", workDir.getAbsolutePath(), e);
        }
    }

    /**
     * 按扩展名清理文件
     */
    private void cleanupFilesByExtension(File dir, Instant expireTime,
                                          CleanupResult result, String... extensions) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                if (matchesExtension(file.getName(), extensions)) {
                    try {
                        FileTime lastModified = Files.getLastModifiedTime(file.toPath());
                        if (lastModified.toInstant().isBefore(expireTime)) {
                            long size = file.length();
                            if (Files.deleteIfExists(file.toPath())) {
                                result.addCleanedFile(file.getAbsolutePath(), size);
                                logger.debug("已删除过期文件: {}", file.getAbsolutePath());
                            } else {
                                result.addFailedFile(file.getAbsolutePath());
                            }
                        }
                    } catch (IOException e) {
                        logger.warn("删除文件失败: {}", file.getAbsolutePath(), e);
                        result.addFailedFile(file.getAbsolutePath());
                    }
                }
            } else if (file.isDirectory()) {
                // 递归处理子目录
                cleanupFilesByExtension(file, expireTime, result, extensions);
            }
        }
    }

    /**
     * 检查文件名是否匹配任一扩展名
     */
    private boolean matchesExtension(String fileName, String... extensions) {
        String lowerName = fileName.toLowerCase();
        for (String ext : extensions) {
            if (lowerName.endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查目录是否为空
     */
    private boolean isDirectoryEmpty(File dir) {
        String[] files = dir.list();
        return files == null || files.length == 0;
    }

    /**
     * 递归删除目录
     */
    private boolean deleteDirectoryRecursively(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!deleteDirectoryRecursively(file)) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    /**
     * 合并清理结果
     */
    private void mergeResult(CleanupResult target, CleanupResult source) {
        target.setFileCount(target.getFileCount() + source.getFileCount());
        target.setDirCount(target.getDirCount() + source.getDirCount());
        target.setFreedSpace(target.getFreedSpace() + source.getFreedSpace());
        target.getCleanedFiles().addAll(source.getCleanedFiles());
        target.getFailedFiles().addAll(source.getFailedFiles());
    }
}
