package com.example.torrentutils.service.impl;

import com.example.torrentutils.model.FileClassificationRule;
import com.example.torrentutils.service.FileClassificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * 文件分类服务实现
 */
@Service
public class FileClassificationServiceImpl implements FileClassificationService {

    private static final Logger logger = LoggerFactory.getLogger(FileClassificationServiceImpl.class);

    /**
     * 分类规则列表
     */
    private final List<FileClassificationRule> classificationRules = new CopyOnWriteArrayList<>();

    public FileClassificationServiceImpl() {
        // 初始化默认分类规则
        initializeDefaultRules();
    }

    /**
     * 初始化默认分类规则
     */
    private void initializeDefaultRules() {
        addRule("fc2ppv", "fc2ppv|fc2-ppv", Pattern.CASE_INSENSITIVE);
        addRule("carib", "加勒比|carib", Pattern.CASE_INSENSITIVE);
        addRule("10-mu", "天然素人|10-mu|10musume", Pattern.CASE_INSENSITIVE);
        addRule("1pondo", "一本道|1pon", Pattern.CASE_INSENSITIVE);
        addRule("heyzo", "heyzo", Pattern.CASE_INSENSITIVE);
        addRule("paco", "paco", Pattern.CASE_INSENSITIVE);
        addRule("kin8", "kin8", Pattern.CASE_INSENSITIVE);
        addRule("c0390", "c0930", Pattern.CASE_INSENSITIVE);
        addRule("h0930", "h0930", Pattern.CASE_INSENSITIVE);
        addRule("h4610", "h4610", Pattern.CASE_INSENSITIVE);
        addRule("AVF-PPV", "AVF-PPV-HEY", Pattern.CASE_INSENSITIVE);
    }

    private void addRule(String categoryName, String regex, int flags) {
        classificationRules.add(new FileClassificationRule(categoryName, regex, flags));
    }

    @Override
    public void classifyFiles(String sourceFolderPath, String targetRootPath) throws IOException {
        File sourceFolder = new File(sourceFolderPath);

        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            throw new IOException("源文件夹不存在或不是一个目录: " + sourceFolderPath);
        }

        File targetRoot = new File(targetRootPath);
        if (!targetRoot.exists()) {
            targetRoot.mkdirs();
        }

        File[] files = sourceFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    classifySingleFile(file, targetRootPath);
                } else {
                    // 递归处理子文件夹
                    classifyFiles(file.getAbsolutePath(), targetRootPath);
                }
            }
        } else {
            logger.info("文件夹为空: {}", sourceFolder.getAbsolutePath());
        }
    }

    @Override
    public boolean classifySingleFile(File file, String targetRootPath) throws IOException {
        String fileName = file.getName();

        for (FileClassificationRule rule : classificationRules) {
            if (rule.matches(fileName)) {
                String category = rule.getCategoryName();
                Path targetDir = Paths.get(targetRootPath, category);

                // 创建目标目录
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                Path targetPath = targetDir.resolve(fileName);

                // 复制文件到分类目录（原逻辑是复制，不是移动）
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                logger.info("已复制文件: {} -> {}", fileName, targetPath);
                return true;
            }
        }

        logger.debug("文件未匹配任何分类规则: {}", fileName);
        return false;
    }

    @Override
    public List<FileClassificationRule> getClassificationRules() {
        return new ArrayList<>(classificationRules);
    }

    @Override
    public void addClassificationRule(FileClassificationRule rule) {
        if (rule != null && rule.getPattern() != null) {
            classificationRules.add(rule);
        }
    }
}
