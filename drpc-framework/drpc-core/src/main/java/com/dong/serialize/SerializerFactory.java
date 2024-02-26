package com.dong.serialize;

import com.dong.compress.CompressorWrapper;
import com.dong.serialize.impl.HessianSerializer;
import com.dong.serialize.impl.JdkSerializer;
import com.dong.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class SerializerFactory {

    private static ConcurrentHashMap<String,SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Byte,SerializerWrapper> SERIALIZER_CACHE_Code = new ConcurrentHashMap<>();

    static {
        SerializerWrapper jdk = new SerializerWrapper((byte)1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte)2, "json", new JsonSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte)3, "hessian", new HessianSerializer());

        SERIALIZER_CACHE.put("jdk",jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE.put("hessian",hessian);

        SERIALIZER_CACHE_Code.put((byte)1,jdk);
        SERIALIZER_CACHE_Code.put((byte)2,json);
        SERIALIZER_CACHE_Code.put((byte)3,hessian);
    }


    /**
     *  使用工厂获取一个SerializerWrapper
     * @param serializerType
     * @return
     */
    public static SerializerWrapper getSerializer(String serializerType){
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE.get(serializerType);
        if(serializerWrapper == null){
            log.error("未找到您配置的【{}】序列化方式，默认选择jdk的序列化方式",serializerType);
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }

    public static SerializerWrapper getSerializer(byte serializerType){
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE_Code.get(serializerType);
        if(serializerWrapper == null){
            log.error("未找到您配置的【{}】反序列化方式，默认选择jdk的反序列化方式",serializerWrapper.getSerializer());
            return SERIALIZER_CACHE_Code.get("jdk");
        }
        return serializerWrapper;
    }
}
