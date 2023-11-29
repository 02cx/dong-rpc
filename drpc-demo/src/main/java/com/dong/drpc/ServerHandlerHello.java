package com.dong.drpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class ServerHandlerHello extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 接受Client的消息
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("Server接收到的消息：" + byteBuf.toString(StandardCharsets.UTF_8));

        // 反馈给 Client 消息
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello，我是服务端，成功接收到消息".getBytes(StandardCharsets.UTF_8)));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
