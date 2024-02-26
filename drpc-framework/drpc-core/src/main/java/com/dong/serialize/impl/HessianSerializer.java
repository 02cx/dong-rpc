package com.dong.serialize.impl;

import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.dong.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.function.LongFunction;

@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if(object == null){
            return null;
        }
        // 序列化
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            HessianOutput output = new HessianOutput(baos);
            output.writeObject(object);
            byte[] bytes = baos.toByteArray();
            log.debug("HessianSerializer对【{}】序列化完成，【{}】",object,bytes.length);
            return bytes;
        } catch (IOException e) {
            log.error("消息序列化失败");
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(data)){
            HessianInput input = new HessianInput(bais);
            Object object = input.readObject();
            return (T)object;
        } catch (Exception e) {
            log.error("请求【{}】的payload反序列化错误！！",data);
            throw new RuntimeException(e);
        }
    }
}
