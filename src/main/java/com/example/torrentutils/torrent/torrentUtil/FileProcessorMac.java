package com.example.torrentutils.torrent.torrentUtil;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 6/30/25 12:08 PM
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileProcessorMac {

    public static void main(String[] args) {
        String folderPath = "/Users/zhangbiao/Documents/视频/video/原始种子文件/2024"; // 替换为实际路径
        //extractWithTheUnarchiver(folderPath,folderPath);
        processFolder(folderPath);
    }
    // 如果安装了The Unarchiver应用
    private static void extractWithTheUnarchiver(String rarPath, String extractPath) {
        try {
            // The Unarchiver的命令行工具路径
            String unarPath = "/opt/homebrew/bin/unar";

            ProcessBuilder pb = new ProcessBuilder( "/opt/homebrew/bin/unar",
                    "-o", extractPath,  // 输出目录
                    "-f",  // 强制覆盖已存在文件
                    rarPath  // 压缩文件路径
                     );
            Process process = pb.start();

            handleProcessOutput(process, "unar");

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("使用The Unarchiver解压成功");
                //return true;
            }
            //return false;
        } catch (Exception e) {
            System.out.println("The Unarchiver不可用: " + e.getMessage());
            //return false;
        }
    }
    public static void processFolder(String folderPath) {
        try {
            Path folder = Paths.get(folderPath);

            if (!Files.exists(folder) || !Files.isDirectory(folder)) {
                System.out.println("指定路径不存在或不是文件夹: " + folderPath);
                return;
            }

            List<Path> txtFiles = new ArrayList<>();
            List<Path> rarFiles = new ArrayList<>();

            Files.walk(folder, 1)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString().toLowerCase();
                        if (fileName.endsWith(".txt")) {
                            txtFiles.add(file);
                        } else if (fileName.endsWith(".rar")) {
                            rarFiles.add(file);
                        }
                    });

            deleteTxtFiles(txtFiles);



            extractRarFiles(rarFiles, folderPath);

        } catch (IOException e) {
            System.err.println("处理文件夹时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteTxtFiles(List<Path> txtFiles) {
        System.out.println("开始删除txt文件...");
        for (Path txtFile : txtFiles) {
            try {
                Files.delete(txtFile);
                System.out.println("已删除: " + txtFile.getFileName());
            } catch (IOException e) {
                System.err.println("删除文件失败: " + txtFile.getFileName() + " - " + e.getMessage());
            }
        }
        System.out.println("txt文件删除完成，共删除 " + txtFiles.size() + " 个文件。");
    }

    private static void deleteFiles(String folderPath) {


    }

    private static void extractRarFiles(List<Path> rarFiles, String extractPath) {
        System.out.println("开始解压rar文件...");
        System.out.println("extractPath: " +extractPath );
        for (Path path:rarFiles
             ) {
            System.out.println(path.getFileName());
        }

        for (Path rarFile : rarFiles) {
            try {
                String rarFileName = rarFile.getFileName().toString();
                String extractFolderName = rarFileName.substring(0, rarFileName.lastIndexOf('.'));
                Path extractFolder = Paths.get(extractPath, extractFolderName);
                Files.createDirectories(extractFolder);

                boolean success = false;

                // 按优先级尝试不同的解压工具
                success = extractWithUnrar(rarFile.toString(), extractFolder.toString());
                if (!success) {
                    success = extractWithSevenZip(rarFile.toString(), extractFolder.toString());
                }
                if (!success) {
                    success = extractWithDitto(rarFile.toString(), extractFolder.toString());
                }

                if (success) {
                    System.out.println("解压成功: " + rarFile.getFileName());
                } else {
                    System.err.println("解压失败: " + rarFile.getFileName() + " (请确保已安装解压工具)");
                    printInstallationInstructions();
                }

            } catch (IOException e) {
                System.err.println("解压文件时发生错误: " + rarFile.getFileName() + " - " + e.getMessage());
            }
        }
        System.out.println("rar文件解压完成，共处理 " + rarFiles.size() + " 个文件。");
    }

    // 使用unrar命令（推荐）
    private static boolean extractWithUnrar(String rarPath, String extractPath) {
        try {
            // The Unarchiver的命令行工具路径
            //String unarPath = "/opt/homebrew/bin/unar";

            ProcessBuilder pb = new ProcessBuilder( "/opt/homebrew/bin/unar",
                    "-o", extractPath,  // 输出目录
                    "-f",  // 强制覆盖已存在文件
                    rarPath  // 压缩文件路径
            );

           // ProcessBuilder pb = new ProcessBuilder("unar", "x", "-o+", rarPath, extractPath);
          //  pb.directory(new File("/opt/homebrew/bin/unar")); // 设置工作目录
            Process process = pb.start();

            // 处理输出
            handleProcessOutput(process, "unar");

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("使用unrar解压成功");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("unrar不可用: " + e.getMessage());
            return false;
        }
    }

    // 使用7z命令（通过Homebrew安装）
    private static boolean extractWithSevenZip(String rarPath, String extractPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("7z", "x", "-y", rarPath, "-o" + extractPath);
            pb.directory(new File(System.getProperty("user.home")));
            Process process = pb.start();

            handleProcessOutput(process, "7z");

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("使用7z解压成功");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("7z不可用: " + e.getMessage());
            return false;
        }
    }

    // 使用ditto命令（Mac内置，但对RAR支持有限）
    private static boolean extractWithDitto(String rarPath, String extractPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("ditto", "-x", "-k", rarPath, extractPath);
            pb.directory(new File(System.getProperty("user.home")));
            Process process = pb.start();

            handleProcessOutput(process, "ditto");

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("使用ditto解压成功");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("ditto不可用或不支持RAR格式: " + e.getMessage());
            return false;
        }
    }

    // 处理进程输出
    private static void handleProcessOutput(Process process, String toolName) {
        try {
            // 处理标准输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(toolName + ": " + line);
            }

            // 处理错误输出
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(toolName + " ERROR: " + line);
            }
        } catch (IOException e) {
            System.err.println("读取进程输出时发生错误: " + e.getMessage());
        }
    }

    // 打印安装说明
    private static void printInstallationInstructions() {
        System.out.println("\n=== Mac环境下RAR解压工具安装说明 ===");
        System.out.println("请选择以下任一工具进行安装：");
        System.out.println();
        System.out.println("1. 安装unrar（推荐）:");
        System.out.println("   使用Homebrew: brew install unrar");
        System.out.println("   使用MacPorts: sudo port install unrar");
        System.out.println();
        System.out.println("2. 安装p7zip（7z命令）:");
        System.out.println("   使用Homebrew: brew install p7zip");
        System.out.println("   使用MacPorts: sudo port install p7zip");
        System.out.println();
        System.out.println("3. 安装The Unarchiver（可选）:");
        System.out.println("   从App Store安装，然后使用命令行工具");
        System.out.println();
        System.out.println("推荐使用Homebrew安装unrar，命令如下：");
        System.out.println("   /bin/bash -c \"$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\"");
        System.out.println("   brew install unrar");
        System.out.println("=====================================\n");
    }
}
