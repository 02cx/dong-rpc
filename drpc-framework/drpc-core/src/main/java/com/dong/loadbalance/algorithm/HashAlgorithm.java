package com.dong.loadbalance.algorithm;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
@Slf4j
public enum HashAlgorithm {
    /**
     * MD5-based hash algorithm used by ketama.
     */
    KETAMA_HASH;

    //用于将字节数组（digest）转换为一个32位的哈希值。
    public long hash(byte[] digest, int nTime) {
        // 与0xFF进行按位与操作，确保结果为无符号的8位整数   左移24位，即将其放置在最高位
        long rv = ((long) (digest[3+nTime*4] & 0xFF) << 24)
                | ((long) (digest[2+nTime*4] & 0xFF) << 16)
                | ((long) (digest[1+nTime*4] & 0xFF) << 8)
                | (digest[0+nTime*4] & 0xFF);
        long res =  rv & 0xffffffffL;
        return res; /* Truncate to 32-bits */
    }

    /**
     * Get the md5 of the given key.
     */
    public byte[] computeMd5(String k) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        // 将 MessageDigest 对象重置为初始状态
        md5.reset();
        byte[] keyBytes = null;
        try {
            //将字符串 k 转换为 UTF-8 编码的字节数组
            keyBytes = k.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown string :" + k, e);
        }
        //将字节数组 keyBytes 传递给 MessageDigest 对象，更新其状态以处理输入数据
        md5.update(keyBytes);
        return md5.digest();
    }
}
