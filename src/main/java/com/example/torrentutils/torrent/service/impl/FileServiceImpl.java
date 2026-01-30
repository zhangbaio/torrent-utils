package com.example.torrentutils.torrent.service.impl;

import com.example.torrentutils.torrent.service.FileService;
import com.example.torrentutils.torrent.service.TorrentService;
import com.example.torrentutils.torrent.torrentUtil.*;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 1/27/26 10:42 AM
 */
@Service
public class FileServiceImpl implements FileService {


    @Value("${app.originalFileDir}")
    private String originalFileDir;

    @Value("${app.allFileDir}")
    private String  allFileDir;

    @Value("${app.classifyFileDir}")
    private String classifyFileDir;

    @Value("${app.uploadTorrentDir}")
    private String uploadTorrentDir;

    private     static Map<Pattern,String> patternStringMap = new HashMap<>();

    static {
        patternStringMap.put(Pattern.compile("fc2ppv|fc2-ppv",Pattern.CASE_INSENSITIVE),"fc2ppv");
        patternStringMap.put(Pattern.compile("加勒比|carib",Pattern.CASE_INSENSITIVE),"carib");
        patternStringMap.put(Pattern.compile("天然素人|10-mu|10musume",Pattern.CASE_INSENSITIVE),"10-mu");
        patternStringMap.put(Pattern.compile("一本道|1pon",Pattern.CASE_INSENSITIVE),"1pondo");
        patternStringMap.put(Pattern.compile("heyzo",Pattern.CASE_INSENSITIVE),"heyzo");
        patternStringMap.put(Pattern.compile("paco",Pattern.CASE_INSENSITIVE),"paco");
        patternStringMap.put(Pattern.compile("kin8",Pattern.CASE_INSENSITIVE),"kin8");
        patternStringMap.put(Pattern.compile("c0930",Pattern.CASE_INSENSITIVE),"c0390");
        patternStringMap.put(Pattern.compile("h0930",Pattern.CASE_INSENSITIVE),"h0930");
        patternStringMap.put(Pattern.compile("h4610",Pattern.CASE_INSENSITIVE),"h4610");
        patternStringMap.put(Pattern.compile("AVF-PPV-HEY",Pattern.CASE_INSENSITIVE),"AVF-PPV");

    }

    private String newFilename;

    public String uploadFile(MultipartFile file, String uploadDir)  {
        if (file.isEmpty()){
            return "请选择文件";
        }

        try {
            // 创建上传目录
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            System.out.println(newFilename);
            // 保存文件
            Path path = Paths.get(uploadDir + originalFilename);
            System.out.println("path" + path.toString());

            Files.write(path, file.getBytes());

            return "文件上传成功: " + newFilename;
        } catch (IOException e) {
            System.out.println("文件上传失败");
            e.printStackTrace();
            return "文件上传失败";
        }

    }
    
    public ResponseEntity uploadMultiZips(MultipartFile[] files, String uploadDir) throws IOException {
        List<String> fileNames = new ArrayList<>();
        Map<String, Object> response = new HashMap<>();
        String newFileDir = uploadDir + UUID.randomUUID().toString() + "/";


        // 创建上传目录
        File dir = new File(newFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            uploadFile(file, newFileDir);

        }
        response.put("message", "文件上传成功");
        response.put("count", files.length);
        response.put("fileNames", fileNames);
        response.put("fileDir", newFileDir);


        // 待解压文件夹路径，待处理种子路径
        String folderPath = newFileDir;

        // 修改文件名称，删除特殊字符
        FileNameModifier.moditifyFileName(folderPath);
        // 删除已经解压的文件
        //FolderCleaner.deleteSubFolders(Paths.get(folderPath));

        //1、 解压文件夹
        FileProcessorMac.processFolder(folderPath);

        String sourceFolderPath = folderPath;

        // 需要移动到 磁力链接种子汇总文件夹路径
        String targetRootPath  = newFileDir + classifyFileDir ;

        //2、对种子文件进行分类，并移动到对应名称的文件夹
        RegexFileUtil.classifyFiles(sourceFolderPath,targetRootPath);

        String targetFolder = newFileDir + allFileDir ;

        File sourceFolder = new File(targetRootPath);

        File[] listFiles = sourceFolder.listFiles();
        if(files != null){
            for (int i = 0; i < listFiles.length; i++) {
                if(listFiles[i].isDirectory()){
                    // System.out.println(files[i].getPath());
                    // 种子文件生成磁力链接汇总文件
                    TorrentToMagnetConverter.convertTorrentsToMagnets(listFiles[i].getPath(),targetFolder);
                }
            }
        }


        return ResponseEntity.ok(response);
    }
    public ResponseEntity uploadMultiTorrent(MultipartFile[] files, String uploadDir) {
        List<String> fileNames = new ArrayList<>();
        Map<String, Object> response = new HashMap<>();
        String newFileDir = uploadDir + UUID.randomUUID().toString() + "/";


        // 创建上传目录
        File dir = new File(newFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            uploadFile(file, newFileDir);

        }
        response.put("message", "文件上传成功");
        response.put("count", files.length);
        response.put("fileNames", fileNames);
        response.put("fileDir", newFileDir);


        final List<String> fileList = TorrentService.convertTorrentsToList(newFileDir);

        response.put("fileLinks", fileList.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取适合当前操作系统的文件路径
     */
    public String resolveFilePath(String uploadPath) {
        // 处理相对路径
        if (!uploadPath.startsWith("./")) {
            String baseDir = System.getProperty("user.dir");
            return Paths.get(baseDir, uploadPath).toString();
        }

        // 处理绝对路径
        return Paths.get(uploadPath).toString();
    }
    /*
    解压文件
     */
    public  void processFolder(String folderPath) {
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
    public   void deleteTxtFiles(List<Path> txtFiles) {
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


    public void extractRarFiles(List<Path> rarFiles, String extractPath) {
        System.out.println("开始解压rar文件...");

        for (Path rarFile : rarFiles) {
            try {
                // 创建解压目录
                String rarFileName = rarFile.getFileName().toString();
                String extractFolderName = rarFileName.substring(0, rarFileName.lastIndexOf('.'));
                Path extractFolder = Paths.get(extractPath, extractFolderName);
                Files.createDirectories(extractFolder);

                // 使用junrar库解压
                Archive archive = new Archive(rarFile.toFile());

                FileHeader fileHeader = archive.nextFileHeader();
                while (fileHeader != null) {
                    File extractedFile = new File(extractFolder.toFile(), fileHeader.getFileName());

                    // 确保父目录存在
                    extractedFile.getParentFile().mkdirs();

                    if (!fileHeader.isDirectory()) {
                        try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                            archive.extractFile(fileHeader, fos);
                        }
                    }

                    fileHeader = archive.nextFileHeader();
                }

                archive.close();
                System.out.println("解压成功: " + rarFile.getFileName());

            } catch (Exception e) {
                System.err.println("解压文件时发生错误: " + rarFile.getFileName() + " - " + e.getMessage());
            }
        }
        System.out.println("rar文件解压完成，共处理 " + rarFiles.size() + " 个文件。");
    }



    public  void classifyFiles(String sourceFolderPath,String targetRootPath) throws IOException {

        File sourceFolder = new File(sourceFolderPath);

        if(!sourceFolder.exists() || !sourceFolder.isDirectory()){
            throw new IOException("源文件夹不存在或不是一个目录: " + sourceFolderPath);
        }

        File targetRoot = new File(targetRootPath);

        if(!targetRoot.exists()){
            targetRoot.mkdirs();
        }

        File[] files = sourceFolder.listFiles();
        if(files != null){
            for (int i = 0; i < files.length; i++) {
                if(!files[i].isDirectory()){
                    classifySingleFile(files[i],targetRootPath);
                }else {
                    classifyFiles(files[i].getAbsolutePath(),targetRootPath) ;
                }
            }
        }else {
            System.out.println("文件夹为空：" + sourceFolder.getAbsolutePath());
        }


    }

    public  void classifySingleFile(File file ,String targetRootPath) throws IOException {

        String fileName = file.getName();
        for(Map.Entry<Pattern,String> entry: patternStringMap.entrySet()){
            // if file matched
            if(entry.getKey().matcher(file.getName()).find()){
                // if  target directory  is not exist;
                String category = entry.getValue();
                final Path targetDir = Paths.get(targetRootPath, category);
                if(!targetDir.toFile().exists()){
                    targetDir.toFile().mkdirs();
                }

                Path targetPath = targetDir.resolve(fileName);

                //Files.move(file.toPath(),targetPath, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(file.toPath(),targetPath, StandardCopyOption.REPLACE_EXISTING);

                System.out.printf("已移动文件: %s -> %s%n", fileName, targetPath);
                return;
            }else {
                System.out.println("正则表达没有匹配：" + entry.getKey().toString());
            }
        }

    }
    public  void deleteSubFolders(Path parentDir) throws IOException {

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
}
