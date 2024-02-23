package com.dong;

import com.dong.discovery.Register;
import com.dong.discovery.RegisterConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReferenceConfig<T> {

    private Class<T> interfaceConsumer;

    private Register register;


    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    // 代理设计模式，生成一个api接口的代理对象
    public T get() {
        // 动态代理
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceConsumer};
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 拉取服务  服务名   返回ip+端口
                List<InetSocketAddress> lookup = register.lookup(interfaceConsumer.getName(), "");
                if (log.isDebugEnabled()) {
                    log.debug("服务调用方从注册中心拉取了服务【{}】", lookup);
                }
                //WYD TODO 2024-02-23:当前只有一个服务
                InetSocketAddress inetSocketAddress = lookup.get(0);
                // 2.用netty连接服务器，发送调用的  服务名+方法名+参数列表，得到结果
                Channel channel = DrpcBootstrap.CHANNEL_CACHE.get(inetSocketAddress);
                if (channel == null) {

                    // 尝试连接服务器，等待连接
                    // channel = NettyBootstrapInitializer.getBootstrap().connect(inetSocketAddress).sync().channel();

                    // 使用addListener执行的异步操作
                    CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
                    NettyBootstrapInitializer.getBootstrap().connect(inetSocketAddress).addListener(
                            (ChannelFutureListener) promise -> {
                                if(promise.isDone()){
                                    // 异步的
                                    log.debug("已经和【{}】建立连接",inetSocketAddress);
                                    channelFuture.complete(promise.channel());
                                }else if(!promise.isSuccess()){
                                    channelFuture.completeExceptionally(promise.cause());
                                }
                            }
                    );
                    // 阻塞获取channel
                     channel = channelFuture.get(3, TimeUnit.SECONDS);
                    // 缓存channel
                    DrpcBootstrap.CHANNEL_CACHE.put(inetSocketAddress, channel);

                }

                CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
                DrpcBootstrap.PENDING_REQUEST.put(1L,objectCompletableFuture);
                ChannelFuture channelFuture = channel.writeAndFlush(Unpooled.copiedBuffer("获取日期".getBytes())).addListener(
                        (ChannelFutureListener) promise -> {
                            if(!promise.isSuccess()){
                                objectCompletableFuture.completeExceptionally(promise.cause());
                            }
                        }
                );


                return objectCompletableFuture.get(3,TimeUnit.SECONDS);
            }
        });

        return (T) helloProxy;
    }


    public Class<T> getInterface() {
        return interfaceConsumer;
    }

    public void setInterface(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }

    public ReferenceConfig() {
    }

    public ReferenceConfig(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }

    public Class<T> getInterfaceConsumer() {
        return interfaceConsumer;
    }

    public void setInterfaceConsumer(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }


}
