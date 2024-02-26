package com.dong.serialize.impl;

import com.dong.serialize.Serializer;
import com.dong.transport.message.RequestPayload;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if(object == null){
            return null;
        }
        // 序列化
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos);) {
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            log.debug("JdkSerializer对【{}】序列化完成，【{}】",object,bytes.length);
            return bytes;
        } catch (IOException e) {
            log.error("消息序列化失败");
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais)){
            Object object = ois.readObject();
            return (T)object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("请求【{}】的payload反序列化错误！！",data);
            throw new RuntimeException(e);
        }
    }
}
