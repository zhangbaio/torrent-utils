package com.example.torrentutils.model;

import java.util.List;

/**
 * 文件处理工作流结果
 */
public class WorkflowResult {

    /**
     * 处理的文件数量
     */
    private int fileCount;

    /**
     * 文件存储目录
     */
    private String fileDir;

    /**
     * 生成的磁力链接列表
     */
    private List<String> magnetLinks;

    /**
     * 分类后的目录列表
     */
    private List<CategoryInfo> categories;

    /**
     * 处理状态
     */
    private String status;

    public static class CategoryInfo {
        private String name;
        private int fileCount;
        private String magnetFile;

        public CategoryInfo(String name, int fileCount, String magnetFile) {
            this.name = name;
            this.fileCount = fileCount;
            this.magnetFile = magnetFile;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getFileCount() {
            return fileCount;
        }

        public void setFileCount(int fileCount) {
            this.fileCount = fileCount;
        }

        public String getMagnetFile() {
            return magnetFile;
        }

        public void setMagnetFile(String magnetFile) {
            this.magnetFile = magnetFile;
        }
    }

    public WorkflowResult() {
    }

    public WorkflowResult(int fileCount, String fileDir, List<String> magnetLinks, List<CategoryInfo> categories, String status) {
        this.fileCount = fileCount;
        this.fileDir = fileDir;
        this.magnetLinks = magnetLinks;
        this.categories = categories;
        this.status = status;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public List<String> getMagnetLinks() {
        return magnetLinks;
    }

    public void setMagnetLinks(List<String> magnetLinks) {
        this.magnetLinks = magnetLinks;
    }

    public List<CategoryInfo> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryInfo> categories) {
        this.categories = categories;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
