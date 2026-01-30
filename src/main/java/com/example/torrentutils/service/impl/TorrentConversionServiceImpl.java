package com.example.torrentutils.service.impl;

import com.example.torrentutils.codec.BencodeDecoder;
import com.example.torrentutils.codec.BencodeEncoder;
import com.example.torrentutils.service.TorrentConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Torrent转换服务实现
 */
@Service
public class TorrentConversionServiceImpl implements TorrentConversionService {

    private static final Logger logger = LoggerFactory.getLogger(TorrentConversionServiceImpl.class);

    @Override
    public List<String> convertTorrentsToList(String folderPath) {
        Path folder = Paths.get(folderPath);

        if (!Files.isDirectory(folder)) {
            logger.error("错误: 指定路径不是文件夹: {}", folderPath);
            return new ArrayList<>();
        }

        List<String> magnetLinks = new ArrayList<>();
        try {
            // 遍历文件夹中的torrent文件
            Files.walk(folder)
                    .filter(path -> path.toString().toLowerCase().endsWith(".torrent"))
                    .forEach(torrentFile -> {
                        try {
                            String magnetLink = parseTorrentToMagnet(torrentFile.toFile());
                            if (magnetLink != null) {
                                magnetLinks.add(magnetLink);
                            }
                        } catch (Exception e) {
                            logger.error("处理文件失败: {} - {}", torrentFile, e.getMessage());
                        }
                    });

            if (magnetLinks.isEmpty()) {
                logger.info("未找到有效的torrent文件");
            }
            logger.info("共处理 {} 个torrent文件", magnetLinks.size());

        } catch (IOException e) {
            logger.error("处理过程中发生错误: {}", e.getMessage());
        }

        return magnetLinks;
    }

    @Override
    public void convertTorrentsToMagnets(String folderPath, String targetFolderPath) {
        Path folder = Paths.get(folderPath);
        Path targetFolder;

        if (targetFolderPath == null) {
            targetFolder = Paths.get(folderPath).getParent();
        } else {
            targetFolder = Paths.get(targetFolderPath);
        }

        File targetRoot = targetFolder.toFile();
        if (!targetRoot.exists()) {
            targetRoot.mkdirs();
        }

        if (!Files.isDirectory(folder)) {
            logger.error("错误: 指定路径不是文件夹: {}", folderPath);
            return;
        }

        String folderName = folder.getFileName().toString();
        String outputFileName = folderName + ".txt";

        try {
            List<String> magnetLinks = new ArrayList<>();

            // 遍历文件夹中的torrent文件
            Files.walk(folder)
                    .filter(path -> path.toString().toLowerCase().endsWith(".torrent"))
                    .forEach(torrentFile -> {
                        try {
                            String magnetLink = parseTorrentToMagnet(torrentFile.toFile());
                            if (magnetLink != null) {
                                magnetLinks.add(magnetLink);
                            }
                        } catch (Exception e) {
                            logger.error("处理文件失败: {} - {}", torrentFile, e.getMessage());
                        }
                    });

            if (magnetLinks.isEmpty()) {
                logger.info("未找到有效的torrent文件");
                return;
            }

            // 写入输出文件
            Path outputPath = targetFolder.resolve(outputFileName);
            Files.write(outputPath, magnetLinks, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            logger.info("成功生成磁力链接文件: {}", outputPath);
            logger.info("共处理 {} 个torrent文件", magnetLinks.size());

        } catch (IOException e) {
            logger.error("处理过程中发生错误: {}", e.getMessage());
        }
    }

    @Override
    public String parseTorrentToMagnet(File torrentFile) {
        try (FileInputStream fis = new FileInputStream(torrentFile)) {
            Map<String, Object> torrentData = BencodeDecoder.decodeToMap(fis);

            if (torrentData == null) {
                return null;
            }

            // 获取info字典
            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) torrentData.get("info");
            if (info == null) {
                return null;
            }

            String infoHash = calculateInfoHashFromFileData(info);
            if (infoHash == null) {
                return null;
            }

            // 获取文件名
            String name = null;
            Object nameObj = info.get("name");
            if (nameObj instanceof byte[]) {
                name = new String((byte[]) nameObj, "UTF-8");
            }

            // 构建磁力链接
            StringBuilder magnet = new StringBuilder();
            magnet.append("magnet:?xt=urn:btih:").append(infoHash);

            if (name != null && !name.trim().isEmpty()) {
                magnet.append("&dn=").append(urlEncode(name));
            }

            return magnet.toString();

        } catch (Exception e) {
            logger.error("解析种子文件失败: {} - {}", torrentFile.getName(), e.getMessage());
            return null;
        }
    }

    @Override
    public String calculateInfoHash(File torrentFile) {
        try (FileInputStream fis = new FileInputStream(torrentFile)) {
            Map<String, Object> torrentData = BencodeDecoder.decodeToMap(fis);

            if (torrentData == null) {
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) torrentData.get("info");
            if (info == null) {
                return null;
            }

            return calculateInfoHashFromFileData(info);

        } catch (Exception e) {
            logger.error("计算info hash失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从info字典计算hash
     */
    private String calculateInfoHashFromFileData(Map<String, Object> info) {
        try {
            byte[] infoBytes = BencodeEncoder.encodeToBytes(info);
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(infoBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("计算hash失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * URL编码
     */
    private String urlEncode(String str) {
        try {
            return java.net.URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            return str;
        }
    }
}
