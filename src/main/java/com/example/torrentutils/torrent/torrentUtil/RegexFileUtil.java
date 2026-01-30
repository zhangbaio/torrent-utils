package com.example.torrentutils.torrent.torrentUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author zhangbiao
 * @version 1.0
 * @description: TODO
 * @date 6/27/25 4:25 PM
 */
public class RegexFileUtil {

    private     static     Map<Pattern,String> patternStringMap = new HashMap<>();

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

    public static void classifyFiles(String sourceFolderPath,String targetRootPath) throws IOException{

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

    public static void classifySingleFile(File file ,String targetRootPath) throws IOException {

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


}
