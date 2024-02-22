package com.dong.discovery.impl;

import com.dong.Constant;
import com.dong.ServiceConfig;
import com.dong.discovery.AbstractRegistry;
import com.dong.utils.NetUtils;
import com.dong.utils.zookeeper.ZookeeperNode;
import com.dong.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class NacosRegister extends AbstractRegistry {

    private ZooKeeper zooKeeper;

    public NacosRegister() {
        zooKeeper = ZookeeperUtils.createZookeeper();
    }

    public NacosRegister(String connectStr, int timeOut) {
        zooKeeper = ZookeeperUtils.createZookeeper(connectStr,timeOut);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        // 服务节点名称
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface();
        // 创建一个持久节点
        if(!ZookeeperUtils.exists(zooKeeper,parentNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }

        // 创建本机临时节点，  ip:port
        //WYD TODO 2024-02-22: 后续处理全局端口问题
        String node = parentNode + "/" + NetUtils.getIpAddress() + ":" + 8088;
        if(!ZookeeperUtils.exists(zooKeeper,node,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }


        log.debug("{}服务已注册", service.toString());
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName, String group) {
        return null;
    }
}
