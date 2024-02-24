package com.dong.channel.handler;

import com.dong.transport.message.DrpcRequest;
import com.dong.transport.message.MessageFormatConstant;
import com.dong.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 消息出站时，经过的第一个处理器
 */
@Slf4j
public class DrpcMessageEncoder extends MessageToByteEncoder<DrpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, DrpcRequest drpcRequest, ByteBuf byteBuf) throws Exception {
        log.debug("消息编码器------------执行");
        // 魔数 3字节
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 版本号 1字节
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 头部长度 2字节
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 总长度
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);
        // 3个类型 3字节
        byteBuf.writeByte(drpcRequest.getRequestType());
        byteBuf.writeByte(drpcRequest.getSerializeType());
        byteBuf.writeByte(drpcRequest.getCompressType());
        // 请求id  8字节
        byteBuf.writeLong(drpcRequest.getRequestId());
        // 消息体
        byte[] body = objectToBytes(drpcRequest.getRequestPayload());
        byteBuf.writeBytes(body);

        // 记录写指针位置
        int writeIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(7);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + body.length);
        // 恢复写指针
        byteBuf.writerIndex(writeIndex);
    }


    private byte[] objectToBytes(RequestPayload requestPayload) {

        // 序列化
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();ObjectOutputStream oos = new ObjectOutputStream(baos);) {
            oos.writeObject(requestPayload);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (IOException e) {
            log.error("消息序列化失败");
            throw new RuntimeException(e);
        }
    }
}
