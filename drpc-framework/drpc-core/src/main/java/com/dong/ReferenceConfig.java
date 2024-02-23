package com.dong;

import com.dong.discovery.Register;
import com.dong.discovery.RegisterConfig;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
@Slf4j
public class ReferenceConfig<T> {

    private Class<T> interfaceConsumer;

    private Register register;



    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    // 代理设计模式，生成一个api接口的代理对象
    public T get() {
        // 动态代理
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceConsumer};
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 拉取服务  服务名   返回ip+端口
                List<InetSocketAddress> lookup = register.lookup(interfaceConsumer.getName(),"");
                if(log.isDebugEnabled()){
                    log.debug("服务调用方从注册中心拉取了服务【{}】",lookup);
                }
                // 2.用netty连接服务器，发送调用的  服务名+方法名+参数列表，得到结果

                return null;
            }
        });

        return (T) helloProxy;
    }



    public Class<T> getInterface() {
        return interfaceConsumer;
    }

    public void setInterface(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }

    public ReferenceConfig() {
    }

    public ReferenceConfig(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }

    public Class<T> getInterfaceConsumer() {
        return interfaceConsumer;
    }

    public void setInterfaceConsumer(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }


}
