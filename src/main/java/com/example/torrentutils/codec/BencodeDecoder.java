package com.example.torrentutils.codec;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bencode解码器
 * 用于解析BitTorrent种子文件格式
 */
public class BencodeDecoder {

    private final PushbackInputStream pis;

    public BencodeDecoder(InputStream is) {
        this.pis = new PushbackInputStream(is, 1);
    }

    /**
     * 解码Bencode数据
     */
    public Object decode() throws IOException {
        int ch = pis.read();
        if (ch == -1) {
            return null;
        }

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

        throw new IOException("无效的Bencode数据: " + (char) ch);
    }

    /**
     * 解码字典类型
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeDict() throws IOException {
        pis.read(); // 跳过 'd'
        Map<String, Object> dict = new HashMap<>();

        while (true) {
            int ch = pis.read();
            if (ch == 'e') {
                break;
            }
            if (ch == -1) {
                throw new IOException("字典未正确结束");
            }

            pis.unread(ch);
            byte[] keyBytes = (byte[]) decodeString();
            String key = new String(keyBytes, "UTF-8");
            Object value = decode();
            dict.put(key, value);
        }

        return dict;
    }

    /**
     * 解码列表类型
     */
    private List<Object> decodeList() throws IOException {
        pis.read(); // 跳过 'l'
        List<Object> list = new ArrayList<>();

        while (true) {
            int ch = pis.read();
            if (ch == 'e') {
                break;
            }
            if (ch == -1) {
                throw new IOException("列表未正确结束");
            }

            pis.unread(ch);
            list.add(decode());
        }

        return list;
    }

    /**
     * 解码整数类型
     */
    private Long decodeInt() throws IOException {
        pis.read(); // 跳过 'i'
        StringBuilder sb = new StringBuilder();

        while (true) {
            int ch = pis.read();
            if (ch == 'e') {
                break;
            }
            if (ch == -1) {
                throw new IOException("整数未正确结束");
            }
            sb.append((char) ch);
        }

        try {
            return Long.parseLong(sb.toString());
        } catch (NumberFormatException e) {
            throw new IOException("无效的整数格式: " + sb.toString(), e);
        }
    }

    /**
     * 解码字符串类型（字节数组）
     */
    private byte[] decodeString() throws IOException {
        StringBuilder lengthStr = new StringBuilder();

        while (true) {
            int ch = pis.read();
            if (ch == ':') {
                break;
            }
            if (ch == -1 || !Character.isDigit(ch)) {
                throw new IOException("字符串长度格式错误");
            }
            lengthStr.append((char) ch);
        }

        try {
            int length = Integer.parseInt(lengthStr.toString());
            byte[] data = new byte[length];
            int totalRead = 0;

            while (totalRead < length) {
                int read = pis.read(data, totalRead, length - totalRead);
                if (read == -1) {
                    throw new IOException("字符串数据不完整");
                }
                totalRead += read;
            }

            return data;
        } catch (NumberFormatException e) {
            throw new IOException("无效的字符串长度: " + lengthStr.toString(), e);
        }
    }

    /**
     * 解码整个输入流为字典类型
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> decodeToMap(InputStream is) throws IOException {
        BencodeDecoder decoder = new BencodeDecoder(is);
        Object result = decoder.decode();
        return (result instanceof Map) ? (Map<String, Object>) result : null;
    }
}
