package com.dong;

import com.dong.channel.handler.DrpcRequestDecoder;
import com.dong.channel.handler.DrpcResponseEncoder;
import com.dong.channel.handler.MethodCallHandler;
import com.dong.core.HeartbeatDetection;
import com.dong.discovery.Register;
import com.dong.discovery.RegisterConfig;
import com.dong.loadbalance.LoadBalance;
import com.dong.loadbalance.impl.ConsistentHashLoadBalance;
import com.dong.loadbalance.impl.MinimumResponseTimeLoadBalance;
import com.dong.transport.message.DrpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hello world!
 */
@Slf4j
public class DrpcBootstrap {

    // DrpcBootstrap是一个单例，一个应用程序一个示例
    public static final DrpcBootstrap drpcBootstrap = new DrpcBootstrap();

    // 定义一些初始配置
    private String applicationName;
    private RegisterConfig registerConfig;
    private ProtocolConfig protocolConfig;
    public static int port = 8082;
    public static final IdGenerator ID_GENERATOR = new IdGenerator(1L,2L);
    public static String SERIALIZE_TYPE = "jdk";
    public static String COMPRESSOR_TYPE = "gzip";

    // 注册中心
    private Register register;

    public static  LoadBalance LOAD_BALANCE;

    // 缓存channel连接
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE  = new ConcurrentHashMap<>(16);

    // 维护已经发布的服务列表   key---->interface全限定名   value---->ServiceConfig
    public static final Map<String,ServiceConfig<?>> SERVER_LIST = new ConcurrentHashMap<>(16);

    // 定义全局的completableFuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(16);

    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    public static final ThreadLocal<DrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();


    public DrpcBootstrap() {
        // 构造启动引导程序时需要做的一些配置
    }

    public static DrpcBootstrap getInstance() {
        return drpcBootstrap;
    }

    /*
        ----------------------------------服务提供方相关api--------------------------------------------------------------------------
     */

    /**
     * 用来定义当前应用的名称
     *
     * @param applicationName 应用名称
     * @return this
     */
    public DrpcBootstrap application(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 用来配置一个注册中心
     *
     * @param registerConfig 注册中心
     * @return this
     */
    public DrpcBootstrap register(RegisterConfig registerConfig) {
        // 类似工厂方法模式
        this.register = registerConfig.getRegister();
        //WYD TODO 2024-02-26: 需要修改负载均衡策略
        LOAD_BALANCE = new MinimumResponseTimeLoadBalance();
        return this;
    }

    /**
     * 配置暴露的服务的协议
     *
     * @param protocolConfig 封装的协议
     * @return this
     */
    public DrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        log.debug("当前工程使用的协议：" + protocolConfig.toString());

        return this;
    }

    /**
     * 发布服务，将接口--->实现，注册到服务中心
     *
     * @param service 封装需要发布的服务
     * @return this
     */
    public DrpcBootstrap publish(ServiceConfig<?> service) {
        // 实现注册
        register.register(service);
        // 服务调用方根据接口名，方法名，参数列表发起调用，服务提供者怎么知道是哪一个实现？
        // 1.new 一个   2.spring   beanFactory.getBean(Class)  3.自己维护映射关系
        SERVER_LIST.put(service.getInterface().getName(),service);

        return this;
    }

    /**
     * 批量发布
     *
     * @param listService 封装需要发布的服务的集合
     * @return this
     */
    public DrpcBootstrap publish(List<ServiceConfig<?>> listService) {
        listService.forEach(service -> {
            this.publish(service);
        });
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        // boss 负责处理请求本身，然后将请求分发给worker
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            // 启动服务器的引导类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 配置服务器
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port)) // 设置监听端口
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 核心   添加发送消息时的处理器
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new DrpcRequestDecoder())
                                    // 根据请求进行方法调用
                                    .addLast(new MethodCallHandler())
                                    .addLast(new DrpcResponseEncoder());
                        }
                    });
            //绑定服务器，该实例将提供有关IO操作的结果或状态的信息
            ChannelFuture future = serverBootstrap.bind().sync();
            log.debug("在" + future.channel().localAddress() + "上开启监听");

            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

     /*
        ----------------------------------服务调用方相关api--------------------------------------------------------------------------
     */

    public DrpcBootstrap reference(ReferenceConfig<?> reference) {
        // 开启对这个服务的心跳检测
        HeartbeatDetection.detectHeartbeat(reference.getInterface().getName());
        // 在这个方法里是否可以拿到相关配置-----注册中心
        // 配置reference，将来调用get时，方便生成代理对象
        reference.setRegister(register);
        return this;
    }


    public DrpcBootstrap serialize(String serializeType) {
        SERIALIZE_TYPE = serializeType;
        return this;
    }

    public DrpcBootstrap compress(String compressType) {
        COMPRESSOR_TYPE = compressType;
        return this;
    }

    public Register getRegister() {
        return register;
    }
}
