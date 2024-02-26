package com.dong.serialize;

import com.dong.serialize.impl.HessianSerializer;
import com.dong.serialize.impl.JdkSerializer;
import com.dong.serialize.impl.JsonSerializer;

import java.util.concurrent.ConcurrentHashMap;

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


    public static SerializerWrapper getSerializer(String serializerType){
        return SERIALIZER_CACHE.get(serializerType);
    }

    public static SerializerWrapper getSerializer(byte serializerType){
        return SERIALIZER_CACHE_Code.get(serializerType);
    }
}
