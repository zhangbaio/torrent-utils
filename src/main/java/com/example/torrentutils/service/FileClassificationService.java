package com.example.torrentutils.service;

import com.example.torrentutils.model.FileClassificationRule;
import java.io.IOException;
import java.util.List;

/**
 * 文件分类服务接口
 */
public interface FileClassificationService {

    /**
     * 对源文件夹中的文件进行分类
     *
     * @param sourceFolderPath 源文件夹路径
     * @param targetRootPath   目标根路径
     * @throws IOException IO异常
     */
    void classifyFiles(String sourceFolderPath, String targetRootPath) throws IOException;

    /**
     * 对单个文件进行分类
     *
     * @param file          文件对象
     * @param targetRootPath 目标根路径
     * @return 是否分类成功
     * @throws IOException IO异常
     */
    boolean classifySingleFile(java.io.File file, String targetRootPath) throws IOException;

    /**
     * 获取所有分类规则
     *
     * @return 分类规则列表
     */
    List<FileClassificationRule> getClassificationRules();

    /**
     * 添加分类规则
     *
     * @param rule 分类规则
     */
    void addClassificationRule(FileClassificationRule rule);
}
