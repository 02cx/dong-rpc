package com.dong;

import com.dong.discovery.RegisterConfig;
import com.dong.loadbalance.LoadBalance;
import com.dong.loadbalance.impl.RoundRobinLoadBalance;
import lombok.Data;

/**
 *  全局配置类，代码配置--->xml配置---->spi配置--->默认项
 */
@Data
public class Configuration {

    // 配置信息--->端口号
    public int port = 8082;

    // 配置信息--->应用名称
    private String applicationName;

    // 配置信息--->注册中心配置
    private RegisterConfig registerConfig;

    // 配置信息--->协议
    private ProtocolConfig protocolConfig;

    // 配置信息--->序列化协议
    private String serializeType = "jdk";
    // 配置信息--->压缩协议
    private String compressorType = "gzip";

    // 配置信息--->ID生成器
    private IdGenerator idGenerator = new IdGenerator(1L, 2L);

    // 配置信息--->负载均衡策略
    private LoadBalance loadBalance = new RoundRobinLoadBalance();

    // 读xml
    public Configuration() {
        // 通过xml获取配置信息
    }
}
