package com.dong.serialize;

public interface Serializer {

    /**
     * 序列化
     * @param object
     * @return
     */
    byte[] serialize(Object object);


    <T> T deserialize(byte[] data,Class<T> clazz);
}
