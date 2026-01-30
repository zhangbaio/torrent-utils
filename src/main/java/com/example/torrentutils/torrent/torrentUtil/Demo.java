package com.example.torrentutils.torrent.torrentUtil;

import java.io.File;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 6/27/25 4:03 PM
 */
public class Demo {
    public static void main(String[] args) {
        File file =  new File("/Users/zhangbiao/Downloads/202401");
        DealFile.traverseFolder(file);
    }
}
