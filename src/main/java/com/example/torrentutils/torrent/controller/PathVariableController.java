/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.torrentutils.torrent.controller;

import com.example.torrentutils.torrent.service.FileService;
import com.example.torrentutils.torrent.service.FolderService;
import com.example.torrentutils.torrent.service.TorrentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */

@RestController
public class PathVariableController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FolderService folderService;

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

    @Value("${app.uploadTorrentDir}")
    private String uploadTorrentDir;


    // 上传多个压缩包，解压，分类移动到文件夹，生成汇总磁力链接文件
    @PostMapping("/uploadMultiZip")
    public ResponseEntity uploadMultiZip(@RequestParam("fileList")MultipartFile[] multipartFiles) throws IOException {
        return fileService.uploadMultiZips(multipartFiles,originalFileDir);

    }
   //上传多个种子文件，返回对应种子到磁力链接
    @PostMapping("/torrent/uploadMultiTorrent")
    public ResponseEntity uploadMultiTorrent(@RequestParam("fileList")MultipartFile[] multipartFiles){
        return fileService.uploadMultiTorrent(multipartFiles,uploadTorrentDir);
    }


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file")MultipartFile file)  {
        System.out.println("upload");
        if (file.isEmpty()){
            return "请选择文件";
        }

        try {
            // 创建上传目录
            File dir = new File(originalFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            System.out.println(newFilename);
            // 保存文件
            Path path = Paths.get(originalFileDir + newFilename);

            Files.write(path, file.getBytes());

            return "文件上传成功: " + newFilename;
        } catch (IOException e) {
            System.out.println("文件上传失败");
            e.printStackTrace();
            return "文件上传失败";
        }


    }

    /*
    解压文件
     */

    @PostMapping("/unzipFile")
    public String unzipFile(@RequestParam("fileName") String fileName) throws IOException {


        return folderService.unZipFile(fileName);

    }
    @PostMapping("/unZipFolder")
    public String unZipFolder(@RequestParam("folderPath") String folderPath) throws IOException {

         folderPath = fileService.resolveFilePath(folderPath); // 替换为实际路径
        File folder = new File(folderPath);
        System.out.println("folderPath"+ folderPath);

        if (folder.exists() && folder.isDirectory()) {
            // 获取文件夹下的所有文件和子文件夹（非递归）
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        System.out.println("文件: " + file.getName());
                        unzipFile(file.getName());
                    } else if (file.isDirectory()) {
                        System.out.println("文件夹: " + file.getName());
                    }
                }
            }
        } else {
            System.out.println("文件夹不存在或不是目录");
        }


        return  "解压成功";

    }

    // http://127.0.0.1:8080/user/123/roles/222
    @RequestMapping(value = "/user/{userId}/roles/{roleId}", method = RequestMethod.GET)
    @ResponseBody
    public String getLogin(@PathVariable("userId") String userId, @PathVariable("roleId") String roleId) {
        return "User Id : " + userId + " Role Id : " + roleId;
    }

    // http://127.0.0.1:8080/javabeat/somewords
    @RequestMapping(value = "/javabeat/{regexp1:[a-z-]+}", method = RequestMethod.GET)
    @ResponseBody
    public String getRegExp(@PathVariable("regexp1") String regexp1) {
        return "URI Part : " + regexp1;
    }
}
