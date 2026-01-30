package com.example.torrentutils.torrent.torrentUtil;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 6/30/25 11:41 AM
 */
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FileNameModifier {

    public static void main(String[] args) {
        String filePath = "/Users/zhangbiao/Documents/视频/video/原始种子文件/2024";
        moditifyFileName(filePath);
    }
    public static void moditifyFileName(String rootPath){
        // 指定要处理的根目录
        File rootDir = new File(rootPath);

        // 定义要删除的字符集合
        Set<Character> charsToRemove = new HashSet<>();
        //charsToRemove.add('\\');
        charsToRemove.add('[');
        charsToRemove.add(']');
        charsToRemove.add('(');
        charsToRemove.add(')');


        // 添加更多需要删除的字符...

        if (!rootDir.exists()) {
            System.out.println("目录不存在: " + rootPath);
            return;
        }

        processDirectory(rootDir, charsToRemove);
        System.out.println("文件名修改完成！");
    }

    private static void processDirectory(File dir, Set<Character> charsToRemove) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归处理子目录
                processDirectory(file, charsToRemove);
            } else {
                // 处理文件
                modifyFileName(file, charsToRemove);
            }
        }
    }

    private static void modifyFileName(File file, Set<Character> charsToRemove) {
        String oldName = file.getName();
        String newName = removeSpecifiedChars(oldName, charsToRemove);

        if (!oldName.equals(newName)) {
            File newFile = new File(file.getParent(), newName);

            // 检查新文件名是否已存在
            if (newFile.exists()) {
                System.out.println("无法重命名，文件已存在: " + newFile.getPath());
                return;
            }

            boolean renamed = file.renameTo(newFile);
            if (renamed) {
                System.out.println("重命名成功: ");
                System.out.println("  原文件名: " + oldName);
                System.out.println("  新文件名: " + newName);
            } else {
                System.out.println("重命名失败: " + file.getPath());
            }
        }
    }

    private static String removeSpecifiedChars(String fileName, Set<Character> charsToRemove) {
        StringBuilder cleanName = new StringBuilder();

        for (char c : fileName.toCharArray()) {
            if (!charsToRemove.contains(c)) {
                cleanName.append(c);
            }
        }

        // 确保文件名不为空
        if (cleanName.length() == 0) {
            cleanName.append("unnamed_file");
        }

        return cleanName.toString();
    }
}
