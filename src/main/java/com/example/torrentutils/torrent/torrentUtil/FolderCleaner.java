package com.example.torrentutils.torrent.torrentUtil;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 6/30/25 1:28 PM
 */
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FolderCleaner {
    public static void deleteSubFolders(Path parentDir) throws IOException {

        if(!parentDir.toFile().exists()){
            return;
        }
        Files.walk(parentDir, 1)  // 只遍历直接子项
                .filter(Files::isDirectory)  // 只处理目录
                .filter(path -> !path.equals(parentDir))  // 排除父目录本身
                .forEach(path -> {
                    try {
                        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                    throws IOException {
                                Files.delete(file);  // 先删除文件
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                    throws IOException {
                                Files.delete(dir);  // 后删除空目录
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void main(String[] args) throws IOException {
        Path targetDir = Paths.get("/path/to/your/directory");
        deleteSubFolders(targetDir);
    }
}
