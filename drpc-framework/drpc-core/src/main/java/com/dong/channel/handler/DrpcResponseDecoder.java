package com.dong.channel.handler;

import com.dong.compress.Compressor;
import com.dong.compress.CompressorFactory;
import com.dong.enumeration.RequestType;
import com.dong.serialize.Serializer;
import com.dong.serialize.SerializerFactory;
import com.dong.transport.message.DrpcRequest;
import com.dong.transport.message.DrpcResponse;
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
public class DrpcResponseDecoder extends LengthFieldBasedFrameDecoder {

    public DrpcResponseDecoder() {
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
      log.debug("-------------客户端收到响应，进行解码");
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
        // 响应状态码
        byte code = byteBuf.readByte();
        // 序列化类型
        byte serializeType = byteBuf.readByte();
        // 压缩类型
        byte compressType = byteBuf.readByte();
        // 请求id
        long requestId = byteBuf.readLong();
        // 封装响应
        DrpcResponse drpcResponse = new DrpcResponse();
        drpcResponse.setSerializeType(serializeType);
        drpcResponse.setCompressType(compressType);
        drpcResponse.setCode(code);
        drpcResponse.setRequestId(requestId);
        // 如果时心跳检测请求，直接返回
        if(requestId == RequestType.HEAD_BEAT.getId()){
            return drpcResponse;
        }

        // 消息体
        int bodyLength = fullLength - headerLength;
        byte[] body = new byte[bodyLength];
        byteBuf.readBytes(body);
        if(body != null && body.length != 0){
            // 解压缩
            Compressor compressor = CompressorFactory.getCompressor(compressType).getCompressor();
            byte[] decompress = compressor.decompress(body);
            // 反序列化
            Serializer serializer = SerializerFactory.getSerializer(serializeType).getSerializer();
            Object object = serializer.deserialize(decompress, Object.class);
            drpcResponse.setBody(object);
        }

        log.debug("通信【{}】在客户端完整解码",drpcResponse.getRequestId());
        return drpcResponse;
    }
}
