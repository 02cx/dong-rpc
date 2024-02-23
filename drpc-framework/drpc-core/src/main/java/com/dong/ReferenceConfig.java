package com.dong;

import com.dong.discovery.Register;
import com.dong.discovery.RegisterConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
                List<InetSocketAddress> lookup = register.lookup(interfaceConsumer.getName(),"");
                if(log.isDebugEnabled()){
                    log.debug("服务调用方从注册中心拉取了服务【{}】",lookup);
                }
                //WYD TODO 2024-02-23:当前只有一个服务
                InetSocketAddress inetSocketAddress = lookup.get(0);
                // 2.用netty连接服务器，发送调用的  服务名+方法名+参数列表，得到结果
                Channel channel = DrpcBootstrap.CHANNEL_CACHE.get(inetSocketAddress);
                if(channel == null){
                    // 客户端干活的线程池 I/O线程池
                    NioEventLoopGroup group = new NioEventLoopGroup();
                    try {
                        // 启动客户端的辅助类
                        Bootstrap bootstrap = new Bootstrap();
                        bootstrap.group(group)
                                .channel(NioSocketChannel.class)// 实例化一个 channel
                                .handler(new ChannelInitializer<SocketChannel>() { // channel 初始化配置
                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                                        // 在pipeline中添加我们自定义的 handler
                                        socketChannel.pipeline().addLast(null);
                                    }
                                });

                        // 尝试连接服务器，等待连接
                         channel = bootstrap.connect(inetSocketAddress).sync().channel();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                ChannelFuture channelFuture = channel.writeAndFlush(new Object());


                return null;
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
