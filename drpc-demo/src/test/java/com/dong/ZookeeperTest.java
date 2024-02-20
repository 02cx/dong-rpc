package com.dong;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ZookeeperTest {

    private ZooKeeper zooKeeper;



    @Test
    @Before
    public void testInitZK()   {
        // 定义连接参数
         //String connectString = "127.0.0.1:2181";
         String connectString = "192.168.183.130:2181";
         // 定义超时时间
         int sessionTimeout = 10000;
        try {
            zooKeeper = new ZooKeeper(connectString,sessionTimeout,null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createZNode(){
        try {
            String s = zooKeeper.create("/dclasss", "hello dong".getBytes(StandardCharsets.UTF_8),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("s = " + s);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if (zooKeeper != null) {
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void testDeleteZNode(){
        try {
            zooKeeper.delete("/dclasss",-1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }finally {
            if(zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void testExistsZNode(){
        try {
            Stat stat = zooKeeper.exists("/dclasss", null);
            zooKeeper.setData("/dclasss","Hi".getBytes(StandardCharsets.UTF_8),-1);

            System.out.println("stat.getVersion() = " + stat.getVersion());
            System.out.println("stat.getAversion() = " + stat.getAversion());
            System.out.println("stat.getCversion() = " + stat.getCversion());
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if(zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
