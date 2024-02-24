package com.dong.channel.handler;

import com.dong.DrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        log.debug("MySimpleChannelInboundHandler--------------执行");
        String result = msg.toString(Charset.defaultCharset());
        CompletableFuture<Object> completableFuture = DrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
