package com.dong;





import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Hello world!
 */
@Slf4j
public class DrpcBootstrap {

    // DrpcBootstrap是一个单例，一个应用程序一个示例
    public static DrpcBootstrap drpcBootstrap = new DrpcBootstrap();

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
        return this;
    }

    /**
     * 用来配置一个注册中心
     *
     * @param registerConfig 注册中心
     * @return this
     */
    public DrpcBootstrap register(RegisterConfig registerConfig) {
        return this;
    }

    /**
     * 配置暴露的服务的协议
     *
     * @param protocolConfig 封装的协议
     * @return this
     */
    public DrpcBootstrap protocol(ProtocolConfig protocolConfig) {
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

        log.debug("{}服务已注册", service.toString());

        return this;
    }

    /**
     * 批量发布
     *
     * @param listService 封装需要发布的服务的集合
     * @return this
     */
    public DrpcBootstrap publish(List<?> listService) {
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {

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
