package com.example.torrentutils.torrent.torrentUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 6/27/25 11:16 PM
 */
public class TorrentUtils {

    public static void main(String[] args) throws IOException {

        String [] fileList = {"2024","2023","2022","2021","2020","2019","2018","2017"};

        String name = "H4610";
//        for (int f = 0; f < fileList.length; f++) {
//
//            String name = fileList[f];

            // 待解压文件夹路径，待处理种子路径
            String folderPath = "/Users/zhangbiao/Documents/视频/video/原始种子文件/" + name; // 替换为实际路径

            // 修改文件名称，删除特殊字符
            FileNameModifier.moditifyFileName(folderPath);
            // 删除已经解压的文件
            FolderCleaner.deleteSubFolders(Paths.get(folderPath));

            //1、 解压文件夹
            FileProcessorMac.processFolder(folderPath);

            String sourceFolderPath = folderPath;

            // 需要移动到 磁力链接种子汇总文件夹路径
            String targetRootPath  = "/Users/zhangbiao/Documents/视频/video/种子分类/" + name;

            //2、对种子文件进行分类，并移动到对应名称的文件夹
            RegexExample.classifyFiles(sourceFolderPath,targetRootPath);

            String targetFolder = "/Users/zhangbiao/Documents/视频/video/磁力链接汇总/"  + name;

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


       // TorrentToMagnetConverter.convertTorrentsToMagnets(targetRootPath);




    }

}
