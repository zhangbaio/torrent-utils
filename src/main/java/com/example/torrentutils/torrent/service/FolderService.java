package com.example.torrentutils.torrent.service;

import com.example.torrentutils.torrent.torrentUtil.FileNameModifier;
import com.example.torrentutils.torrent.torrentUtil.TorrentToMagnetConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 1/12/26 9:07 PM
 */
@Service
public class FolderService {

    @Value("${app.uploadDir}")
    private String uploadBaseDir;

    @Autowired
    private FileService fileService;

    @Autowired
    private TorrentService torrentService;
    // 配置上传目录（可以在application.properties中配置）
    @Value("${app.uploadDir}")
    private  String uploadDir  ;

    @Value("${app.originalFileDir}")
    private String originalFileDir;

    @Value("${app.allFileDir}")
    private String  allFileDir;

    @Value("${app.classifyFileDir}")
    private String classifyFileDir;




    public void saveFolder(MultipartFile[] files, String relativePath) throws IOException {
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            // 获取文件相对路径
            String originalFilename = file.getOriginalFilename();
            String filePath = StringUtils.hasText(relativePath)
                    ? relativePath + "/" + originalFilename
                    : originalFilename;

            // 创建目录结构
            Path targetPath = Paths.get(uploadBaseDir, filePath).normalize();
            Files.createDirectories(targetPath.getParent());

            // 保存文件
            file.transferTo(targetPath.toFile());
        }
    }

    public String unZipFile(String fileName) throws IOException {
        String name = "H4610";

        name = fileName;

//        for (int f = 0; f < fileList.length; f++) {
//
//            String name = fileList[f];

        // 待解压文件夹路径，待处理种子路径
        String folderPath = fileService.resolveFilePath(originalFileDir); // 替换为实际路径
        System.out.println("folderPath: " + folderPath);
        // 修改文件名称，删除特殊字符
        FileNameModifier.moditifyFileName(folderPath);
        // 删除已经解压的文件
        fileService.deleteSubFolders(Paths.get(folderPath));

        //1、 解压文件夹
        fileService.processFolder(folderPath);

        String sourceFolderPath = folderPath;

        // 需要移动到 磁力链接种子汇总文件夹路径
        String targetRootPath  = fileService.resolveFilePath(originalFileDir );

        //2、对种子文件进行分类，并移动到对应名称的文件夹
        fileService.classifyFiles(sourceFolderPath,targetRootPath);

        String targetFolder =   fileService.resolveFilePath(originalFileDir);

        File sourceFolder = new File(targetRootPath);

        File[] files = sourceFolder.listFiles();
        if(files != null){
            for (int i = 0; i < files.length; i++) {
                if(files[i].isDirectory()){
                    // System.out.println(files[i].getPath());
                    // 种子文件生成磁力链接汇总文件
                    TorrentToMagnetConverter.convertTorrentsToMagnets(files[i].getPath(),targetFolder);
                }
            }
        }
        // }
        return  "解压成功";

    }


}