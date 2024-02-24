package com.dong;

import com.dong.channel.ConsumerChannelInitializer;
import com.dong.channel.handler.MySimpleChannelInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供 Bootstrap单例
 */
@Slf4j
public class NettyBootstrapInitializer {

    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        // 客户端干活的线程池 I/O线程池
        NioEventLoopGroup group = new NioEventLoopGroup();
        // 启动客户端的辅助类
        bootstrap.group(group)
                .channel(NioSocketChannel.class)// 实例化一个 channel
                .handler(new ConsumerChannelInitializer());
    }

    // 静态代码块中的内容写在方法中时，当某个时间有多个线程同时访问方法时，可能会会配置多次
    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
