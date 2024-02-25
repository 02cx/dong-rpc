package com.dong.channel.handler;

import com.dong.DrpcBootstrap;
import com.dong.transport.message.DrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<DrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DrpcResponse drpcResponse) throws Exception {
        // 服务提供方，响应的结果
        Object body = drpcResponse.getBody();
        CompletableFuture<Object> completableFuture = DrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(body);
        log.debug("通信【{}】在客户端获得了响应",drpcResponse.getRequestId());
    }
}
