package com.example.torrentutils.service.workflow;

import com.example.torrentutils.config.FilePathProperties;
import com.example.torrentutils.model.FileClassificationRule;
import com.example.torrentutils.model.WorkflowResult;
import com.example.torrentutils.service.*;
import com.example.torrentutils.torrent.torrentUtil.FileNameModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件处理工作流服务
 * 编排多步骤的文件处理流程
 */
@Service
public class FileProcessingWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessingWorkflow.class);

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private FileExtractionService fileExtractionService;

    @Autowired
    private FileClassificationService fileClassificationService;

    @Autowired
    private TorrentConversionService torrentConversionService;

    @Autowired
    private FilePathProperties filePathProperties;

    /**
     * 处理上传的压缩包文件
     * 流程：上传 → 修改文件名 → 解压 → 分类 → 生成磁力链接
     *
     * @param multipartFiles 上传的文件数组
     * @return 处理结果
     */
    public WorkflowResult processUploadedZips(MultipartFile[] multipartFiles) throws IOException {
        // 创建唯一的工作目录
        String workDir = filePathProperties.getOriginalFileDir() + UUID.randomUUID().toString() + "/";

        try {
            // 1. 上传文件
            List<String> uploadedPaths = fileUploadService.uploadMultipleFiles(multipartFiles, workDir);
            logger.info("上传了 {} 个文件到 {}", uploadedPaths.size(), workDir);

            // 2. 修改文件名，删除特殊字符
            FileNameModifier.moditifyFileName(workDir);

            // 3. 解压文件
            fileExtractionService.processFolder(workDir);
            logger.info("解压完成");

            // 4. 分类文件
            String classifyDir = workDir + filePathProperties.getClassifyFileDir();
            fileClassificationService.classifyFiles(workDir, classifyDir);
            logger.info("文件分类完成");

            // 5. 生成磁力链接汇总
            String magnetSummaryDir = workDir + filePathProperties.getAllFileDir();
            List<WorkflowResult.CategoryInfo> categories = generateMagnetSummaries(classifyDir, magnetSummaryDir);

            // 6. 构建结果
            WorkflowResult result = new WorkflowResult();
            result.setFileCount(multipartFiles.length);
            result.setFileDir(workDir);
            result.setCategories(categories);
            result.setStatus("处理完成");

            return result;

        } catch (Exception e) {
            logger.error("处理文件时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 处理上传的种子文件
     * 流程：上传 → 转换为磁力链接
     *
     * @param multipartFiles 上传的文件数组
     * @return 处理结果
     */
    public WorkflowResult processUploadedTorrents(MultipartFile[] multipartFiles) throws IOException {
        // 创建唯一的工作目录
        String workDir = filePathProperties.getUploadTorrentDir() + UUID.randomUUID().toString() + "/";

        try {
            // 1. 上传文件
            List<String> uploadedPaths = fileUploadService.uploadMultipleFiles(multipartFiles, workDir);
            logger.info("上传了 {} 个种子文件到 {}", uploadedPaths.size(), workDir);

            // 2. 转换为磁力链接列表
            List<String> magnetLinks = torrentConversionService.convertTorrentsToList(workDir);
            logger.info("生成了 {} 个磁力链接", magnetLinks.size());

            // 3. 构建结果
            WorkflowResult result = new WorkflowResult();
            result.setFileCount(multipartFiles.length);
            result.setFileDir(workDir);
            result.setMagnetLinks(magnetLinks);
            result.setStatus("处理完成");

            return result;

        } catch (Exception e) {
            logger.error("处理种子文件时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 为每个分类生成磁力链接汇总文件
     *
     * @param classifyDir      分类目录
     * @param magnetSummaryDir 磁力链接汇总目录
     * @return 分类信息列表
     */
    private List<WorkflowResult.CategoryInfo> generateMagnetSummaries(String classifyDir, String magnetSummaryDir) {
        List<WorkflowResult.CategoryInfo> categories = new ArrayList<>();

        File sourceFolder = new File(classifyDir);
        if (!sourceFolder.exists()) {
            return categories;
        }

        // 创建汇总目录
        new File(magnetSummaryDir).mkdirs();

        File[] folders = sourceFolder.listFiles();
        if (folders != null) {
            for (File folder : folders) {
                if (folder.isDirectory()) {
                    // 生成该分类的磁力链接汇总文件
                    torrentConversionService.convertTorrentsToMagnets(folder.getPath(), magnetSummaryDir);

                    // 统计信息
                    String categoryName = folder.getName();
                    int fileCount = countTorrentFiles(folder);
                    String magnetFile = magnetSummaryDir + categoryName + ".txt";

                    categories.add(new WorkflowResult.CategoryInfo(categoryName, fileCount, magnetFile));

                    logger.info("分类 {} 处理完成，{} 个文件", categoryName, fileCount);
                }
            }
        }

        return categories;
    }

    /**
     * 统计文件夹中的torrent文件数量
     */
    private int countTorrentFiles(File folder) {
        File[] files = folder.listFiles();
        if (files == null) {
            return 0;
        }

        int count = 0;
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".torrent")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取所有分类规则
     */
    public List<FileClassificationRule> getClassificationRules() {
        return fileClassificationService.getClassificationRules();
    }

    /**
     * 添加分类规则
     */
    public void addClassificationRule(FileClassificationRule rule) {
        fileClassificationService.addClassificationRule(rule);
    }
}
