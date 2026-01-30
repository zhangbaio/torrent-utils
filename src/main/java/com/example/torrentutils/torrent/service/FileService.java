package com.example.torrentutils.torrent.service;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
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
 * @date 1/12/26 7:44 PM
 */
@Service
public interface FileService {

    public String uploadFile(MultipartFile file, String uploadDir);
    public ResponseEntity uploadMultiTorrent(MultipartFile[] files, String uploadDir) ;

    public ResponseEntity uploadMultiZips(MultipartFile[] files, String uploadDir) throws IOException;

    public String resolveFilePath(String uploadPath);
    public  void processFolder(String folderPath);
    public   void deleteTxtFiles(List<Path> txtFiles);
    public void extractRarFiles(List<Path> rarFiles, String extractPath);
    public  void classifyFiles(String sourceFolderPath,String targetRootPath) throws IOException;
    public  void classifySingleFile(File file ,String targetRootPath) throws IOException;
    public  void deleteSubFolders(Path parentDir) throws IOException;

}
