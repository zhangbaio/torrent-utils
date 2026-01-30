package com.example.torrentutils.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 文件分类规则配置类
 * 从 application.yml 读取分类规则配置
 */
@Component
@ConfigurationProperties(prefix = "app.classification")
public class ClassificationRuleProperties {

    /**
     * 分类规则列表
     */
    private List<Rule> rules = new ArrayList<>();

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * 分类规则
     */
    public static class Rule {
        /**
         * 分类名称
         */
        private String name;

        /**
         * 匹配模式
         */
        private String pattern;

        /**
         * 是否忽略大小写，默认为 true
         */
        private boolean caseInsensitive = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public boolean isCaseInsensitive() {
            return caseInsensitive;
        }

        public void setCaseInsensitive(boolean caseInsensitive) {
            this.caseInsensitive = caseInsensitive;
        }

        /**
         * 编译为 Pattern 对象
         */
        public Pattern compilePattern() {
            int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
            return Pattern.compile(pattern, flags);
        }
    }
}
