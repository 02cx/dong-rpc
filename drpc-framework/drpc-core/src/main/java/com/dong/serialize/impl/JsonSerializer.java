package com.dong.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.dong.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        byte[] bytes = JSON.toJSONBytes(object);
        log.debug("JsonSerializer【{}】序列化完成，【{}】",object,bytes.length);
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        T t = JSON.parseObject(data, clazz);
        log.debug("类【{}】反序列化完成",clazz);
        return t;
    }
}
