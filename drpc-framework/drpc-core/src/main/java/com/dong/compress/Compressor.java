package com.dong.compress;

public interface Compressor {

    /**
     * 压缩
     * @param data 原数据
     * @return 压缩后的数据
     */
    byte[] compress(byte[] data);

    /**
     * 解压缩
     * @param data 压缩后的数据
     * @return 原数据
     */
    byte[] decompress(byte[] data);
}
