package com.music.utils;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.Arrays;

public class NcmDecoder {

    // 网易云音乐 NCM 文件固定的 AES 密钥
    private static final byte[] CORE_KEY = new byte[]{
            0x68, 0x7A, 0x48, 0x52, 0x41, 0x6D, 0x73, 0x6F,
            0x35, 0x6B, 0x49, 0x6E, 0x62, 0x40, 0x33, 0x56
    };
    private static final byte[] META_KEY = new byte[]{
            0x23, 0x31, 0x34, 0x6C, 0x6A, 0x6B, 0x2F, 0x50,
            0x45, 0x3C, 0x30, 0x77, 0x31, 0x34, 0x4B, 0x79
    };

    public static File decode(File ncmFile, String outputDir) throws Exception {
        try (FileInputStream fis = new FileInputStream(ncmFile);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            // 1. 跳过 NCM 文件头 (10字节)
            byte[] header = new byte[10];
            bis.read(header);
            if (!Arrays.equals(header, new byte[]{0x43, 0x54, 0x45, 0x4E, 0x46, 0x44, 0x41, 0x4D, 0x00, 0x00})) {
                throw new IllegalArgumentException("不是有效的 NCM 文件");
            }

            // 2. 读取 RC4 密钥长度
            byte[] keyLenBytes = new byte[4];
            bis.read(keyLenBytes);
            int keyLen = byteArrayToInt(keyLenBytes);

            // 3. 读取 RC4 密钥数据
            byte[] rc4KeyData = new byte[keyLen];
            bis.read(rc4KeyData);
            // 解密 RC4 密钥
            byte[] rc4Key = aesDecrypt(rc4KeyData, CORE_KEY);
            // 去掉前7字节 "neteasecloudmusic"
            rc4Key = Arrays.copyOfRange(rc4Key, 7, rc4Key.length);

            // 4. 读取 Meta 数据长度
            byte[] metaLenBytes = new byte[4];
            bis.read(metaLenBytes);
            int metaLen = byteArrayToInt(metaLenBytes);

            // 5. 读取 Meta 数据 (可选，用于解析歌曲信息)
            if (metaLen > 0) {
                byte[] metaData = new byte[metaLen];
                bis.read(metaData);
                // 这里可以解析 Meta 数据获取歌曲信息
            }

            // 6. 跳过 CRC 和 图片数据 (直接定位到音频数据)
            byte[] crcBytes = new byte[4];
            bis.read(crcBytes);
            byte[] imageSizeBytes = new byte[4];
            bis.read(imageSizeBytes);
            int imageSize = byteArrayToInt(imageSizeBytes);
            bis.skip(imageSize);

            // 7. 解密音频数据并输出 MP3
            String outputFileName = ncmFile.getName().replace(".ncm", ".mp3");
            File mp3File = new File(outputDir, outputFileName);
            try (FileOutputStream fos = new FileOutputStream(mp3File);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                byte[] buffer = new byte[0x8000];
                RC4 rc4 = new RC4(rc4Key);
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    rc4.crypt(buffer, len);
                    bos.write(buffer, 0, len);
                }
            }
            return mp3File;
        }
    }

    // AES 解密
    private static byte[] aesDecrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    // 字节数组转 int (小端序)
    private static int byteArrayToInt(byte[] bytes) {
        return (bytes[0] & 0xFF) |
                ((bytes[1] & 0xFF) << 8) |
                ((bytes[2] & 0xFF) << 16) |
                ((bytes[3] & 0xFF) << 24);
    }

    // RC4 实现
    static class RC4 {
        private final byte[] S = new byte[256];
        private int x = 0, y = 0;

        RC4(byte[] key) {
            for (int i = 0; i < 256; i++) {
                S[i] = (byte) i;
            }
            int j = 0;
            for (int i = 0; i < 256; i++) {
                j = (j + S[i] + key[i % key.length]) & 0xFF;
                byte temp = S[i];
                S[i] = S[j];
                S[j] = temp;
            }
        }

        void crypt(byte[] data, int len) {
            for (int i = 0; i < len; i++) {
                x = (x + 1) & 0xFF;
                y = (y + S[x]) & 0xFF;
                byte temp = S[x];
                S[x] = S[y];
                S[y] = temp;
                data[i] ^= S[(S[x] + S[y]) & 0xFF];
            }
        }
    }
}