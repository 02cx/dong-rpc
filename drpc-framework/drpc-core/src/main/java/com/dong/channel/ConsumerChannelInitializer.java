package com.dong.channel;

import com.dong.channel.handler.DrpcRequestEncoder;
import com.dong.channel.handler.DrpcResponseDecoder;
import com.dong.channel.handler.MySimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        log.debug("ConsumerChannelInitializer---------执行");
        socketChannel.pipeline()
                // netty自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                // 消息编码器
                .addLast(new DrpcRequestEncoder())
                // 入站
                .addLast(new DrpcResponseDecoder())
                .addLast(new MySimpleChannelInboundHandler());
    }
}
