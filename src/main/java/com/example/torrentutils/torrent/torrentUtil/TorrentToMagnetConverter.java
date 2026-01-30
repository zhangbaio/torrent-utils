package com.example.torrentutils.torrent.torrentUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TorrentToMagnetConverter {

    public static void main(String[] args) {

        String folderPath = "/Users/zhangbiao/Documents/视频/video/2025/H4610";
        String targetPath = "/Users/zhangbiao/Documents/视频/video/磁力链接汇总";
        convertTorrentsToMagnets(folderPath,targetPath);
    }


    public static void convertTorrentsToMagnets(String folderPath,String targetFolderPath) {
        Path folder = Paths.get(folderPath);
        Path targetFolder = null;
        if(targetFolderPath == null){
            targetFolder = Paths.get(folderPath).getParent();
        }else{
            targetFolder =  Paths.get(targetFolderPath);
        }

        File targetRoot = targetFolder.toFile();

        if(!targetRoot.exists()){
            targetRoot.mkdirs();
        }


        if (!Files.isDirectory(folder)) {
            System.err.println("错误: 指定路径不是文件夹");
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
                            System.err.println("处理文件失败: " + torrentFile + " - " + e.getMessage());
                        }
                    });

            if (magnetLinks.isEmpty()) {
                System.out.println("未找到有效的torrent文件");
                return;
            }

            // 写入输出文件
            Path outputPath = targetFolder.resolve(outputFileName);
            Files.write(outputPath, magnetLinks, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("成功生成磁力链接文件: " + outputPath);
            System.out.println("共处理 " + magnetLinks.size() + " 个torrent文件");

        } catch (IOException e) {
            System.err.println("处理过程中发生错误: " + e.getMessage());
        }
    }
    public static void convertListToFile(List<String> fileList,String targetFolderPath,String fileName) throws IOException {
        Path targetFolder = null;
        if(targetFolderPath != null){
            targetFolder =  Paths.get(targetFolderPath);
        }

        File targetRoot = targetFolder.toFile();

        if(!targetRoot.exists()){
            targetRoot.mkdirs();
        }
        String folderName = fileName;
        String outputFileName = folderName + ".txt";
        // 写入输出文件
        Path outputPath = targetFolder.resolve(outputFileName);
        Files.write(outputPath, fileList, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);


    }
    public static List<String> convertTorrentsToList(String folderPath) {
        Path folder = Paths.get(folderPath);


        if (!Files.isDirectory(folder)) {
            System.err.println("错误: 指定路径不是文件夹");
        }

        String folderName = folder.getFileName().toString();
        String outputFileName = folderName + ".txt";
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
                            System.err.println("处理文件失败: " + torrentFile + " - " + e.getMessage());
                        }
                    });

            if (magnetLinks.isEmpty()) {
                System.out.println("未找到有效的torrent文件");
            }
            System.out.println("共处理 " + magnetLinks.size() + " 个torrent文件");

        } catch (IOException e) {
            System.err.println("处理过程中发生错误: " + e.getMessage());
        }
        return magnetLinks;
    }

    private static String parseTorrentToMagnet(File torrentFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(torrentFile)) {
            Map<String, Object> torrentData = decodeBencode(fis);

            if (torrentData == null) {
                return null;
            }

            // 获取info字典并计算hash
            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) torrentData.get("info");
            if (info == null) {
                return null;
            }

            String infoHash = calculateInfoHash(info);
            if (infoHash == null) {
                return null;
            }

            // 获取文件名
            String name = null;
            Object nameObj = info.get("name");
            if (nameObj instanceof byte[]) {
                name = new String((byte[]) nameObj, "UTF-8");
            }

            // 构建简洁的磁力链接，只包含hash和文件名
            StringBuilder magnet = new StringBuilder();
            magnet.append("magnet:?xt=urn:btih:").append(infoHash);

            if (name != null && !name.trim().isEmpty()) {
                magnet.append("&dn=").append(urlEncode(name));
            }

            // 不添加任何tracker信息，保持链接简洁
            return magnet.toString();
        }
    }

    private static String calculateInfoHash(Map<String, Object> info) {
        try {
            byte[] infoBytes = encodeBencode(info);
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(infoBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String urlEncode(String str) {
        try {
            return java.net.URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            return str;
        }
    }

    // 简化的Bencode解码器
    @SuppressWarnings("unchecked")
    private static Map<String, Object> decodeBencode(InputStream is) throws IOException {
        BencodeDecoder decoder = new BencodeDecoder(is);
        Object result = decoder.decode();
        return (result instanceof Map) ? (Map<String, Object>) result : null;
    }

    // 简化的Bencode编码器
    private static byte[] encodeBencode(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BencodeEncoder encoder = new BencodeEncoder(baos);
        encoder.encode(obj);
        return baos.toByteArray();
    }

    // Bencode解码器实现
    static class BencodeDecoder {
        private final PushbackInputStream pis;

        public BencodeDecoder(InputStream is) {
            this.pis = new PushbackInputStream(is, 1);
        }

        public Object decode() throws IOException {
            int ch = pis.read();
            if (ch == -1) return null;

            pis.unread(ch);

            if (ch == 'd') {
                return decodeDict();
            } else if (ch == 'l') {
                return decodeList();
            } else if (ch == 'i') {
                return decodeInt();
            } else if (Character.isDigit(ch)) {
                return decodeString();
            }

            throw new IOException("无效的Bencode数据");
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> decodeDict() throws IOException {
            pis.read(); // 跳过 'd'
            Map<String, Object> dict = new HashMap<>();

            while (true) {
                int ch = pis.read();
                if (ch == 'e') break;
                if (ch == -1) throw new IOException("字典未正确结束");

                pis.unread(ch);
                byte[] keyBytes = (byte[]) decodeString();
                String key = new String(keyBytes, "UTF-8");
                Object value = decode();
                dict.put(key, value);
            }

            return dict;
        }

        private List<Object> decodeList() throws IOException {
            pis.read(); // 跳过 'l'
            List<Object> list = new ArrayList<>();

            while (true) {
                int ch = pis.read();
                if (ch == 'e') break;
                if (ch == -1) throw new IOException("列表未正确结束");

                pis.unread(ch);
                list.add(decode());
            }

            return list;
        }

        private Long decodeInt() throws IOException {
            pis.read(); // 跳过 'i'
            StringBuilder sb = new StringBuilder();

            while (true) {
                int ch = pis.read();
                if (ch == 'e') break;
                if (ch == -1) throw new IOException("整数未正确结束");
                sb.append((char) ch);
            }

            return Long.parseLong(sb.toString());
        }

        private byte[] decodeString() throws IOException {
            StringBuilder lengthStr = new StringBuilder();

            while (true) {
                int ch = pis.read();
                if (ch == ':') break;
                if (ch == -1 || !Character.isDigit(ch)) {
                    throw new IOException("字符串长度格式错误");
                }
                lengthStr.append((char) ch);
            }

            int length = Integer.parseInt(lengthStr.toString());
            byte[] data = new byte[length];
            int totalRead = 0;

            while (totalRead < length) {
                int read = pis.read(data, totalRead, length - totalRead);
                if (read == -1) throw new IOException("字符串数据不完整");
                totalRead += read;
            }

            return data;
        }
    }

    // Bencode编码器实现
    static class BencodeEncoder {
        private final OutputStream os;

        public BencodeEncoder(OutputStream os) {
            this.os = os;
        }

        @SuppressWarnings("unchecked")
        public void encode(Object obj) throws IOException {
            if (obj instanceof Map) {
                encodeDict((Map<String, Object>) obj);
            } else if (obj instanceof List) {
                encodeList((List<Object>) obj);
            } else if (obj instanceof Long || obj instanceof Integer) {
                encodeInt(((Number) obj).longValue());
            } else if (obj instanceof byte[]) {
                encodeBytes((byte[]) obj);
            } else if (obj instanceof String) {
                encodeBytes(((String) obj).getBytes("UTF-8"));
            } else {
                throw new IOException("不支持的数据类型: " + obj.getClass());
            }
        }

        private void encodeDict(Map<String, Object> dict) throws IOException {
            os.write('d');

            // 按键排序
            List<String> sortedKeys = dict.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());

            for (String key : sortedKeys) {
                encodeBytes(key.getBytes("UTF-8"));
                encode(dict.get(key));
            }

            os.write('e');
        }

        private void encodeList(List<Object> list) throws IOException {
            os.write('l');
            for (Object item : list) {
                encode(item);
            }
            os.write('e');
        }

        private void encodeInt(long value) throws IOException {
            os.write('i');
            os.write(String.valueOf(value).getBytes());
            os.write('e');
        }

        private void encodeBytes(byte[] data) throws IOException {
            os.write(String.valueOf(data.length).getBytes());
            os.write(':');
            os.write(data);
        }
    }
}