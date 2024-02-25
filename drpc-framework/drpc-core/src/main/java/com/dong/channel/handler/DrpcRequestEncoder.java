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
public class DrpcRequestEncoder extends MessageToByteEncoder<DrpcRequest> {
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
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_LENGTH_LENGTH);
        // 3个类型 3字节
        byteBuf.writeByte(drpcRequest.getRequestType());
        byteBuf.writeByte(drpcRequest.getSerializeType());
        byteBuf.writeByte(drpcRequest.getCompressType());
        // 请求id  8字节
        byteBuf.writeLong(drpcRequest.getRequestId());

        // 心跳请求，不写入请求体
/*        if(drpcRequest.getRequestType() == RequestType.HEAD_BEAT.getId()){
            return;
        }*/

        // 消息体
        byte[] body = objectToBytes(drpcRequest.getRequestPayload());
        byteBuf.writeBytes(body);
        int bodyLength = body == null ? 0 : body.length;

        // 记录写指针位置
        int writeIndex = byteBuf.writerIndex();
        // 写指针移动到记录总长度Full_length处
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length  +MessageFormatConstant.VERSION + MessageFormatConstant.HEADER_LENGTH_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        // 恢复写指针
        byteBuf.writerIndex(writeIndex);

        log.debug("通信【{}】在客户端完整编码",drpcRequest.getRequestId());
    }


    private byte[] objectToBytes(RequestPayload requestPayload) {
        if(requestPayload == null){
            return null;
        }
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
