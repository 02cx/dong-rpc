package com.dong.channel.handler;

import com.dong.enumeration.RequestType;
import com.dong.transport.message.DrpcRequest;
import com.dong.transport.message.MessageFormatConstant;
import com.dong.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * 基于字段长度的帧解析器
 */
@Slf4j
public class DrpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public DrpcMessageDecoder() {
        // 找到当前报文的总长度，截取报文，进行解析
        super(
                // 最大帧的长度，超过maxFrameLength就会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH,
                // 长度的字段的偏移量
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_LENGTH_LENGTH,
                // 长度的字段的长度
                MessageFormatConstant.FULL_LENGTH_LENGTH,
                // 负载的适配长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_LENGTH_LENGTH + MessageFormatConstant.FULL_LENGTH_LENGTH)
                , 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if(decode instanceof ByteBuf byteBuf){
            return decodeFrame(byteBuf);
        }
        return null;

    }

    private Object decodeFrame(ByteBuf byteBuf){
        // 魔数
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        for (int i = 0; i < magic.length; i++) {
            if(magic[i] != MessageFormatConstant.MAGIC[i]){
                throw new RuntimeException("请求报文格式错误");
            }
        }

        // 版本号
        byte version = byteBuf.readByte();
        if(version > MessageFormatConstant.VERSION){
            throw new RuntimeException("请求版本号不一致");
        }
        // 头部长度
        short headerLength = byteBuf.readShort();
        // 总长度
        int fullLength = byteBuf.readInt();
        // 请求类型
        byte requestType = byteBuf.readByte();
        // 序列化类型
        byte serializeType = byteBuf.readByte();
        // 压缩类型
        byte compressType = byteBuf.readByte();
        // 请求id
        long requestId = byteBuf.readLong();
        // 封装请求
        DrpcRequest drpcRequest = new DrpcRequest();
        drpcRequest.setRequestType(requestType);
        drpcRequest.setSerializeType(serializeType);
        drpcRequest.setCompressType(compressType);
        drpcRequest.setRequestId(requestId);
        // 如果时心跳检测请求，直接返回
        if(requestId == RequestType.HEAD_BEAT.getId()){
            return drpcRequest;
        }

        // 消息体
        int payloadLength = fullLength - headerLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);
        // 反序列化
        try(ByteArrayInputStream bais = new ByteArrayInputStream(payload);
            ObjectInputStream ois = new ObjectInputStream(bais)){
            RequestPayload requestPayload = (RequestPayload) ois.readObject();
            drpcRequest.setRequestPayload(requestPayload);
            log.debug("解析的报文：{}",drpcRequest);
        } catch (IOException | ClassNotFoundException e) {
            log.error("请求【{}】的payload反序列化错误！！",requestId);
            throw new RuntimeException(e);
        }
        return drpcRequest;
    }
}
