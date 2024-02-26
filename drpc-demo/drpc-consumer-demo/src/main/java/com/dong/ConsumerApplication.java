package com.dong;


import com.dong.discovery.RegisterConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Hello world!
 *
 */
@Slf4j
public class ConsumerApplication {

    public static void main(String[] args) {

        // 想尽办法获取代理对象，使用ReferenceConfig进行封装
        // referenceConfig一定用生成代理的模板方法，get()
        ReferenceConfig<HelloDrpc> reference = new ReferenceConfig();
        reference.setInterface(HelloDrpc.class);

        // 代理作用：
        // 1.连接注册中心
        // 2.拉取注册服务列表
        // 3.选择一个服务并建立连接
        // 4.发送请求，携带一些信息（接口名，方法名，参数列表），获得结果
        DrpcBootstrap.getInstance()
                .application("first-drpc-consumer")   // 应用名称
                .register(new RegisterConfig("zookeeper://192.168.183.130:2181"))  // 注册中心
                .serialize("jdk")
                .reference(reference);

        // 获取代理对象
        HelloDrpc helloDrpc = reference.get();
        String result = helloDrpc.sayHi("hi drpc");
        log.info("服务消费者接收到的消息：{}",result);
    }
}
