package com.example.torrentutils.service;

import com.example.torrentutils.model.CleanupResult;

/**
 * 文件清理服务接口
 */
public interface FileCleanupService {

    /**
     * 清理过期的临时文件（使用配置的延迟时间）
     *
     * @return 清理结果
     */
    CleanupResult cleanupExpiredFiles();

    /**
     * 清理过期的临时文件
     *
     * @param delayHours 文件保留多少小时后清理
     * @return 清理结果
     */
    CleanupResult cleanupExpiredFiles(int delayHours);

    /**
     * 清理指定工作目录
     *
     * @param workDir 工作目录路径
     * @return 清理结果
     */
    CleanupResult cleanupWorkDir(String workDir);

    /**
     * 手动触发清理
     *
     * @param delayHours 文件保留多少小时后清理，null 表示使用配置值
     * @return 清理结果
     */
    CleanupResult manualCleanup(Integer delayHours);
}
