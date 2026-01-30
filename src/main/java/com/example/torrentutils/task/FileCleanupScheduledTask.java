package com.example.torrentutils.task;

import com.example.torrentutils.config.CleanupProperties;
import com.example.torrentutils.model.CleanupResult;
import com.example.torrentutils.service.FileCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 文件清理定时任务
 */
@Component
public class FileCleanupScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(FileCleanupScheduledTask.class);

    @Autowired
    private CleanupProperties cleanupProperties;

    @Autowired
    private FileCleanupService fileCleanupService;

    /**
     * 定时清理过期文件
     * 执行时间由 app.cleanup.cron 配置
     */
    @Scheduled(cron = "${app.cleanup.cron:0 0 2 * * ?}")
    public void cleanupExpiredFiles() {
        if (!cleanupProperties.isEnabled()) {
            logger.debug("文件清理任务已禁用，跳过执行");
            return;
        }

        logger.info("开始执行文件清理定时任务");
        long startTime = System.currentTimeMillis();

        try {
            CleanupResult result = fileCleanupService.cleanupExpiredFiles();

            long duration = System.currentTimeMillis() - startTime;
            logger.info("文件清理定时任务完成: 删除 {} 个文件, {} 个目录, 释放 {}, 耗时 {} ms",
                    result.getFileCount(), result.getDirCount(), result.getFormattedFreedSpace(), duration);

        } catch (Exception e) {
            logger.error("文件清理定时任务执行失败: {}", e.getMessage(), e);
        }
    }
}
