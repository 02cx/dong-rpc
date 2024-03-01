package com.dong;

import com.dong.compress.Compressor;
import com.dong.compress.impl.GZipCompressor;
import com.dong.discovery.RegisterConfig;
import com.dong.loadbalance.LoadBalance;
import com.dong.loadbalance.impl.RoundRobinLoadBalance;
import com.dong.serialize.Serializer;
import com.dong.serialize.impl.JdkSerializer;
import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.lang.reflect.InvocationTargetException;

/**
 *  全局配置类，代码配置--->xml配置---->spi配置--->默认项
 */
@Data
public class Configuration {

    // 配置信息--->端口号
    public int port = 8082;

    // 配置信息--->应用名称
    private String applicationName = "default";

    // 配置信息--->注册中心配置
    private RegisterConfig registerConfig = new RegisterConfig("zookeeper://192.168.183.130:2181");

    // 配置信息--->协议
    private ProtocolConfig protocolConfig = new ProtocolConfig("jdk");

    // 配置信息--->序列化协议
    private String serializeType = "jdk";
    private Serializer serializer = new JdkSerializer();

    // 配置信息--->压缩协议
    private String compressorType = "gzip";
    private Compressor compressor = new GZipCompressor();

    // 配置信息--->ID生成器
    private IdGenerator idGenerator  = new IdGenerator(1L, 2L);

    // 配置信息--->负载均衡策略
    private LoadBalance loadBalance = new RoundRobinLoadBalance();

    // 读xml，dom4j
    public Configuration() {
        // 通过xml获取配置信息
        loadFromXml(this);
    }

    /**
     *  从xml中读取配置
     * @param configuration
     */
    private void loadFromXml(Configuration configuration) {
        try {
            // 读取xml，获取document对象
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("drpc-config.xml"));
            // 解析xml中的配置，并覆盖默认值
            resolveXML(document,configuration);
        } catch (DocumentException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void resolveXML(Document document,Configuration configuration) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // 解析port
        Element element = (Element) document.selectSingleNode("/configuration/port");
        int port = (element == null ? configuration.getPort() : Integer.parseInt(element.getText()));
        configuration.setPort(port);
        // 解析applicationName
        element = (Element) document.selectSingleNode("/configuration/applicationName");
        String applicationName = (element == null ? configuration.getApplicationName() : element.getText());
        configuration.setApplicationName(applicationName);
        // 解析register
        element = (Element) document.selectSingleNode("/configuration/register");
        if(element != null){
            String url = element.attributeValue("url");
            configuration.setRegisterConfig(new RegisterConfig(url));
        }
        //解析serializeType
        element = (Element) document.selectSingleNode("/configuration/serializeType");
        if(element != null){
            String type = element.attributeValue("type");
            configuration.setSerializeType(type);
        }
        //解析serialize
        element = (Element) document.selectSingleNode("/configuration/serialize");
        if(element != null){
            String clazzPath = element.attributeValue("class");
            Class<?> clazz = Class.forName(clazzPath);
            Serializer instance = (Serializer) clazz.getConstructor().newInstance();
            configuration.setSerializer(instance);
        }
        //解析compressorType
        element = (Element) document.selectSingleNode("/configuration/compressorType");
        if(element != null){
            String type = element.attributeValue("type");
            configuration.setCompressorType(type);
        }
        //解析compressor
        element = (Element) document.selectSingleNode("/configuration/compressor");
        if(element != null){
            String clazzPath = element.attributeValue("class");
            Class<?> clazz = Class.forName(clazzPath);
            Compressor instance = (Compressor) clazz.getConstructor().newInstance();
            configuration.setCompressor(instance);
        }
        //解析loadBalance
        element = (Element) document.selectSingleNode("/configuration/loadBalance");
        if(element != null){
            String clazzPath = element.attributeValue("class");
            Class<?> clazz = Class.forName(clazzPath);
            LoadBalance instance = (LoadBalance) clazz.getConstructor().newInstance();
            configuration.setLoadBalance(instance);
        }
        //解析idGenerator
        element = (Element) document.selectSingleNode("/configuration/idGenerator");
        if(element != null){
            String clazzPath = element.attributeValue("class");
            String dataCenterId = element.attributeValue("dataCenterId");
            String machineId = element.attributeValue("machineId");
            Class<?> clazz = Class.forName(clazzPath);
            IdGenerator instance = (IdGenerator) clazz.getConstructor(Long.class, Long.class).newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
            configuration.setIdGenerator(instance);
        }
    }





    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        System.out.println(configuration);
        configuration.loadFromXml(configuration);
        System.out.println(configuration);
    }
}
