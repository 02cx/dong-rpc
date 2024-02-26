package com.dong.compress.impl;

import com.dong.compress.Compressor;
import com.dong.exceptions.CompressException;
import com.dong.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 使用GZIP算法压缩进行实现
 */
@Slf4j
public class GZipCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] data) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream(); GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)){
            gzipOutputStream.write(data);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();

            log.debug("数据【{}】压缩完成，长度【{}】----->【{}】",data,data.length,result.length);

            return result;
        } catch (IOException e) {
            log.error("数据【{}】压缩异常",data);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] data) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(data); GZIPInputStream gzipInputStream = new GZIPInputStream(bais)){
            byte[] result = gzipInputStream.readAllBytes();

            log.debug("数据【{}】解压缩完成，长度【{}】----->【{}】",data,data.length,result.length);

            return result;
        } catch (IOException e) {
            log.error("数据【{}】解压缩异常",data);
            throw new CompressException(e);
        }
    }
}
