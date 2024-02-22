package com.dong;

/**
 * Hello world!
 */
public class Constant {

    // zookeeper的默认连接地址
    public static final String DEFAULT_ZK_CONNECT = "192.168.183.130:2181";
    // 连接zk超时时间
    public static final int TIME_OUT = 10000;
    // provider节点父路径
    public static final String BASE_PROVIDER_PATH = "/drpc-metadata/providers";
    // consumer
    public static final String BASE_CONSUMER_PATH = "/drpc-metadata/consumers";


}
