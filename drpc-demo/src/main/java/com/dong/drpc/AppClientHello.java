package com.dong.drpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class AppClientHello {
    private final String host;
    private final int port;

    public AppClientHello(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        // 客户端干活的线程池 I/O线程池
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            // 启动客户端的辅助类
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)// 实例化一个 channel
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() { // channel 初始化配置
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 在pipeline中添加我们自定义的 handler
                            socketChannel.pipeline().addLast(new ClientHandlerHello());
                        }
                    });

            // 尝试连接服务器，等待连接
            ChannelFuture channelFuture = bootstrap.connect().sync();
            // 发送消息到服务端
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("Hello Server"
                    .getBytes(StandardCharsets.UTF_8)));
            //阻塞操作，closeFuture()开启了一个channel的监听器（这期间channel在进行各项工作），直到链路断开
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new AppClientHello("127.0.0.1",8080).run();
    }
}
