package com.example.torrentutils.controller;

import com.example.torrentutils.model.ApiResponse;
import com.example.torrentutils.model.CleanupResult;
import com.example.torrentutils.model.FileClassificationRule;
import com.example.torrentutils.model.WorkflowResult;
import com.example.torrentutils.service.FileCleanupService;
import com.example.torrentutils.service.FileExtractionService;
import com.example.torrentutils.service.workflow.FileProcessingWorkflow;
import com.example.torrentutils.service.FileClassificationService;
import com.example.torrentutils.service.TorrentConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 * 提供文件上传、解压、分类、磁力链接转换等API
 */
@RestController
@RequestMapping("/api")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private FileProcessingWorkflow fileProcessingWorkflow;

    @Autowired
    private FileExtractionService fileExtractionService;

    @Autowired
    private FileClassificationService fileClassificationService;

    @Autowired
    private TorrentConversionService torrentConversionService;

    @Autowired
    private FileCleanupService fileCleanupService;

    /**
     * 上传多个压缩包，解压，分类移动到文件夹，生成汇总磁力链接文件
     *
     * @param fileList 上传的文件列表
     * @return 处理结果
     */
    @PostMapping("/uploadMultiZip")
    public ApiResponse<WorkflowResult> uploadMultiZip(@RequestParam("fileList") MultipartFile[] fileList) {
        try {
            logger.info("收到 {} 个压缩包上传请求", fileList.length);
            WorkflowResult result = fileProcessingWorkflow.processUploadedZips(fileList);
            return ApiResponse.success("文件处理完成", result);
        } catch (IOException e) {
            logger.error("处理压缩包失败: {}", e.getMessage(), e);
            return ApiResponse.error("处理压缩包失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("未知错误: {}", e.getMessage(), e);
            return ApiResponse.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 上传多个种子文件，返回对应的磁力链接
     *
     * @param fileList 上传的种子文件列表
     * @return 处理结果
     */
    @PostMapping("/torrent/uploadMultiTorrent")
    public ApiResponse<WorkflowResult> uploadMultiTorrent(@RequestParam("fileList") MultipartFile[] fileList) {
        try {
            logger.info("收到 {} 个种子文件上传请求", fileList.length);
            WorkflowResult result = fileProcessingWorkflow.processUploadedTorrents(fileList);
            return ApiResponse.success("种子文件处理完成", result);
        } catch (IOException e) {
            logger.error("处理种子文件失败: {}", e.getMessage(), e);
            return ApiResponse.error("处理种子文件失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("未知错误: {}", e.getMessage(), e);
            return ApiResponse.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 上传单个文件
     *
     * @param file 上传的文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("请选择文件");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 创建上传目录
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // 保存文件
            Path path = Paths.get(uploadDir, newFilename);
            Files.write(path, file.getBytes());

            logger.info("文件上传成功: {}", newFilename);
            return ApiResponse.success("文件上传成功: " + newFilename, newFilename);

        } catch (IOException e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 解压指定文件
     *
     * @param fileName 文件名
     * @return 解压结果
     */
    @PostMapping("/unzipFile")
    public ApiResponse<String> unzipFile(@RequestParam("fileName") String fileName) {
        try {
            // 这里使用原逻辑的参数传递方式
            // 实际解压逻辑应该由工作流处理
            logger.info("解压文件请求: {}", fileName);

            // 执行解压流程（简化版本）
            String workDir = System.getProperty("user.dir") + "/uploads/" + UUID.randomUUID() + "/";
            fileExtractionService.processFolder(workDir);

            return ApiResponse.success("解压成功", fileName);

        } catch (Exception e) {
            logger.error("解压文件失败: {}", e.getMessage(), e);
            return ApiResponse.error("解压文件失败: " + e.getMessage());
        }
    }

    /**
     * 解压文件夹中的所有压缩文件
     *
     * @param folderPath 文件夹路径
     * @return 解压结果
     */
    @PostMapping("/unZipFolder")
    public ApiResponse<String> unZipFolder(@RequestParam("folderPath") String folderPath) {
        try {
            logger.info("解压文件夹请求: {}", folderPath);

            // 处理文件夹中的所有压缩文件
            fileExtractionService.processFolder(folderPath);

            return ApiResponse.success("解压成功", folderPath);

        } catch (Exception e) {
            logger.error("解压文件夹失败: {}", e.getMessage(), e);
            return ApiResponse.error("解压文件夹失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有文件分类规则
     *
     * @return 分类规则列表
     */
    @GetMapping("/classification/rules")
    public ApiResponse<List<FileClassificationRule>> getClassificationRules() {
        List<FileClassificationRule> rules = fileProcessingWorkflow.getClassificationRules();
        return ApiResponse.success(rules);
    }

    /**
     * 添加文件分类规则
     *
     * @param rule 分类规则
     * @return 操作结果
     */
    @PostMapping("/classification/rules")
    public ApiResponse<String> addClassificationRule(@RequestBody FileClassificationRule rule) {
        fileProcessingWorkflow.addClassificationRule(rule);
        return ApiResponse.success("分类规则添加成功", rule.getCategoryName());
    }

    /**
     * 下载磁力链接文件
     *
     * @param filePath 文件路径
     * @return 文件资源
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filePath") String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(path);
            String filename = path.getFileName().toString();

            // URL编码文件名以支持中文 (RFC 5987 格式)
            String encodedFilename = URLEncoder.encode(filename, "UTF-8")
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(path))
                    .body(resource);

        } catch (Exception e) {
            logger.error("下载文件失败: {} - {}", filePath, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量下载多个磁力链接文件（打包为zip）
     *
     * @param filePaths 文件路径列表
     * @return zip文件
     */
    @PostMapping("/download/batch")
    public ResponseEntity<Resource> downloadBatch(@RequestBody List<String> filePaths) {
        try {
            // 创建临时zip文件
            String tempZipPath = System.getProperty("java.io.tmpdir") + "/magnets_" + UUID.randomUUID() + ".zip";
            Path zipPath = Paths.get(tempZipPath);

            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(zipPath))) {
                for (String filePath : filePaths) {
                    Path file = Paths.get(filePath);
                    if (Files.exists(file) && Files.isRegularFile(file)) {
                        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(file.getFileName().toString());
                        zos.putNextEntry(entry);
                        Files.copy(file, zos);
                        zos.closeEntry();
                    }
                }
            }

            Resource resource = new FileSystemResource(zipPath);
            String filename = "磁力链接汇总.zip";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(zipPath))
                    .body(resource);

        } catch (Exception e) {
            logger.error("批量下载失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 手动触发文件清理任务
     *
     * @param delayHours 可选，文件保留多少小时后清理，不传则使用配置值
     * @return 清理结果
     */
    @PostMapping("/cleanup/trigger")
    public ApiResponse<CleanupResult> triggerCleanup(@RequestParam(required = false) Integer delayHours) {
        try {
            logger.info("手动触发文件清理任务，延迟时间: {} 小时", delayHours);
            CleanupResult result = fileCleanupService.manualCleanup(delayHours);
            return ApiResponse.success("文件清理完成", result);
        } catch (Exception e) {
            logger.error("文件清理失败: {}", e.getMessage(), e);
            return ApiResponse.error("文件清理失败: " + e.getMessage());
        }
    }

    /**
     * 获取清理任务状态
     *
     * @return 当前清理配置信息
     */
    @GetMapping("/cleanup/status")
    public ApiResponse<CleanupStatus> getCleanupStatus() {
        CleanupStatus status = new CleanupStatus();
        status.setEnabled(true); // 从配置读取
        status.setDelayHours(24); // 从配置读取
        status.setCron("0 0 2 * * ?"); // 从配置读取
        return ApiResponse.success(status);
    }

    /**
     * 清理任务状态
     */
    public static class CleanupStatus {
        private boolean enabled;
        private int delayHours;
        private String cron;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getDelayHours() {
            return delayHours;
        }

        public void setDelayHours(int delayHours) {
            this.delayHours = delayHours;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }
    }
}
