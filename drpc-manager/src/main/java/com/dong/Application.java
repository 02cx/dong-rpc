package com.dong;

/**
 * Hello world!
 */

import com.dong.exceptions.ZookeeperException;
import com.dong.utils.zookeeper.ZookeeperNode;
import com.dong.utils.zookeeper.ZookeeperUtils;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;

/**
 * 注册中心的管理页面
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        // 创建基础目录
        // 创建一个zookeep实例
        ZooKeeper zookeeper = ZookeeperUtils.createZookeeper();
        // 定义节点和数据
        String basePath = "/drpc-metadata";
        String providerPath = basePath + "/providers";
        String consumerPath = basePath + "/consumers";

        ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
        ZookeeperNode providerNode = new ZookeeperNode(providerPath , null);
        ZookeeperNode consumerNode = new ZookeeperNode(consumerPath, null);

        // 创建节点
        List.of(baseNode, providerNode, consumerNode).forEach(node -> {
            ZookeeperUtils.createNode(zookeeper,node,null,CreateMode.PERSISTENT);
        });

        ZookeeperUtils.close(zookeeper);

    }

}
