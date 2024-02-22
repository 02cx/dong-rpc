package com.dong;





import com.dong.discovery.Register;
import com.dong.discovery.RegisterConfig;
import com.dong.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
    private int port = 8088;

    private Register register;


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
        register.register(service);
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
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

     /*
        ----------------------------------服务调用方相关api--------------------------------------------------------------------------
     */

    public DrpcBootstrap reference(ReferenceConfig<?> referenceConfig) {
        // 在这个方法里是否可以拿到相关配置-----注册中心
        // 配置reference，将来调用get时，方便生成代理对象

        return this;
    }


}
