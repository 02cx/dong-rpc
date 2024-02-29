package com.dong;

import com.dong.discovery.RegisterConfig;
import com.dong.impl.HelloDrpcImpl;

public class ProviderApplication {

    public static void main(String[] args) {
        // 服务提供方，需要注册服务，启动服务
        // 1.封装要发布的服务
        ServiceConfig<HelloDrpc> serviceConfig = new ServiceConfig();
        serviceConfig.setInterface(HelloDrpc.class);
        serviceConfig.setRef(new HelloDrpcImpl());
        // 2.定义注册中心

        // 3.通过启动引导程序，启动服务提供方
        //  配置---发布
        DrpcBootstrap.getInstance()
                .application("first-drpc-provider")   // 应用名称
                .register(new RegisterConfig("zookeeper://192.168.183.130:2181"))  // 注册中心
                .protocol(new ProtocolConfig("jdk"))  // 协议
                //.publish(serviceConfig)  // 发布服务
                .scan("com.dong") // 扫包进行批量发布
                .start();  // 启动服务
    }
}
