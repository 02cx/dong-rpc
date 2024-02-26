package com.dong.compress;

import com.dong.compress.impl.GZipCompressor;
import com.dong.serialize.SerializerWrapper;
import com.dong.serialize.impl.HessianSerializer;
import com.dong.serialize.impl.JdkSerializer;
import com.dong.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class CompressorFactory {

    private static ConcurrentHashMap<String, CompressorWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Byte,CompressorWrapper> COMPRESSOR_CACHE_Code = new ConcurrentHashMap<>();

    static {
        CompressorWrapper gzip = new CompressorWrapper((byte) 1, "gzip", new GZipCompressor());
        COMPRESSOR_CACHE.put("gzip",gzip);
        COMPRESSOR_CACHE_Code.put((byte)1,gzip);
    }


    /**
     *  使用工厂获取一个CompressorWrapper
     * @param compressorType
     * @return
     */
    public static CompressorWrapper getCompressor(String compressorType){
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE.get(compressorType);
        if(compressorWrapper == null){
            log.error("未找到您配置的【{}】压缩策略，默认选择gzip压缩策略",compressorType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorWrapper;
    }

    public static CompressorWrapper getCompressor(byte compressorType){
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE_Code.get(compressorType);
        if(compressorWrapper == null){
            log.error("未找到您配置的【{}】解压缩策略，默认选择gzip解压缩策略",compressorWrapper.getCompressor());
            return COMPRESSOR_CACHE_Code.get((byte)1);
        }
        return compressorWrapper;
    }
}
