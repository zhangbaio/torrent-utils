package com.example.torrentutils.model;

import java.util.regex.Pattern;

/**
 * 文件分类规则
 */
public class FileClassificationRule {

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 匹配模式
     */
    private Pattern pattern;

    public FileClassificationRule(String categoryName, Pattern pattern) {
        this.categoryName = categoryName;
        this.pattern = pattern;
    }

    public FileClassificationRule(String categoryName, String regex, int flags) {
        this.categoryName = categoryName;
        this.pattern = Pattern.compile(regex, flags);
    }

    public FileClassificationRule(String categoryName, String regex) {
        this(categoryName, regex, 0);
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * 判断文件名是否匹配此规则
     */
    public boolean matches(String fileName) {
        return pattern.matcher(fileName).find();
    }
}
