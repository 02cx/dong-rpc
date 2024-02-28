package com.dong.channel.handler;

import com.dong.compress.Compressor;
import com.dong.compress.CompressorFactory;
import com.dong.serialize.Serializer;
import com.dong.serialize.SerializerFactory;
import com.dong.transport.message.DrpcRequest;
import com.dong.transport.message.DrpcResponse;
import com.dong.transport.message.MessageFormatConstant;
import com.dong.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import javax.security.sasl.SaslServerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 消息出站时，经过的第一个处理器
 */
@Slf4j
public class DrpcResponseEncoder extends MessageToByteEncoder<DrpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext ctx, DrpcResponse drpcResponse, ByteBuf byteBuf) throws Exception {
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
        byteBuf.writeByte(drpcResponse.getCode());
        byteBuf.writeByte(drpcResponse.getSerializeType());
        byteBuf.writeByte(drpcResponse.getCompressType());
        // 请求id  8字节
        byteBuf.writeLong(drpcResponse.getRequestId());

        // 消息体
        byte[] body = null;
        if(drpcResponse.getBody() != null){
            // 序列化
            Serializer serializer = SerializerFactory.getSerializer(drpcResponse.getSerializeType()).getSerializer();
            body = serializer.serialize(drpcResponse.getBody());

            // 压缩
            Compressor compressor = CompressorFactory.getCompressor(drpcResponse.getCompressType()).getCompressor();
            body = compressor.compress(body);
        }
        if(body != null ){
            byteBuf.writeBytes(body);
        }

        int bodyLength = body == null ? 0 : body.length;

        // 记录写指针位置
        int writeIndex = byteBuf.writerIndex();
        // 写指针移动到记录总长度Full_length处
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length  +MessageFormatConstant.VERSION + MessageFormatConstant.HEADER_LENGTH_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        // 恢复写指针
        byteBuf.writerIndex(writeIndex);

        log.debug("通信【{}】在服务端完整编码",drpcResponse.getRequestId());
        log.debug("响应内容：{}",drpcResponse);
    }


}
