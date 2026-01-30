package com.example.torrentutils.torrent.torrentUtil;

import java.io.File;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 6/27/25 3:56 PM
 */
public class DealFile {

    public static void traverseFolder(File folder){
        if(folder.exists() && folder.isDirectory()){
             File[] files = folder.listFiles();
             if(files != null){
                 for (int i = 0; i < files.length; i++) {
                     if(!files[i].isDirectory()){
                         System.out.println(files[i].getName());
                     }else {
                         traverseFolder(files[i]) ;
                     }
                 }
             }
        }else {
            System.out.println("无效的文件夹" + folder.getAbsolutePath());
        }

    }

    public static void categoryFiles(){

    }
    public static void uploadFile(){

    }
}
