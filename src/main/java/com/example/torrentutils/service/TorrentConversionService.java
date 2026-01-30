package com.example.torrentutils.service;

import java.io.File;
import java.util.List;

/**
 * Torrent转换服务接口
 * 提供种子文件转换为磁力链接的功能
 */
public interface TorrentConversionService {

    /**
     * 将文件夹中的torrent文件转换为磁力链接列表
     *
     * @param folderPath 文件夹路径
     * @return 磁力链接列表
     */
    List<String> convertTorrentsToList(String folderPath);

    /**
     * 将文件夹中的torrent文件转换为磁力链接并保存到文件
     *
     * @param folderPath       种子文件夹路径
     * @param targetFolderPath 目标文件夹路径（null表示保存到同级目录）
     */
    void convertTorrentsToMagnets(String folderPath, String targetFolderPath);

    /**
     * 将单个torrent文件转换为磁力链接
     *
     * @param torrentFile 种子文件
     * @return 磁力链接字符串
     */
    String parseTorrentToMagnet(File torrentFile);

    /**
     * 计算种子文件的info hash
     *
     * @param torrentFile 种子文件
     * @return info hash（大写十六进制）
     */
    String calculateInfoHash(File torrentFile);
}
