package com.example.torrentutils.codec;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bencode编码器
 * 用于编码BitTorrent种子文件格式
 */
public class BencodeEncoder {

    private final OutputStream os;

    public BencodeEncoder(OutputStream os) {
        this.os = os;
    }

    /**
     * 编码对象为Bencode格式
     */
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

    /**
     * 编码字典类型
     */
    private void encodeDict(Map<String, Object> dict) throws IOException {
        os.write('d');

        // 按键排序（Bencode规范要求）
        List<String> sortedKeys = dict.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        for (String key : sortedKeys) {
            encodeBytes(key.getBytes("UTF-8"));
            encode(dict.get(key));
        }

        os.write('e');
    }

    /**
     * 编码列表类型
     */
    private void encodeList(List<Object> list) throws IOException {
        os.write('l');
        for (Object item : list) {
            encode(item);
        }
        os.write('e');
    }

    /**
     * 编码整数类型
     */
    private void encodeInt(long value) throws IOException {
        os.write('i');
        os.write(String.valueOf(value).getBytes());
        os.write('e');
    }

    /**
     * 编码字节类型
     */
    private void encodeBytes(byte[] data) throws IOException {
        os.write(String.valueOf(data.length).getBytes());
        os.write(':');
        os.write(data);
    }

    /**
     * 将对象编码为字节数组
     */
    public static byte[] encodeToBytes(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BencodeEncoder encoder = new BencodeEncoder(baos);
        encoder.encode(obj);
        return baos.toByteArray();
    }
}
