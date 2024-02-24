package com.dong;

import com.dong.discovery.Register;
import com.dong.proxy.handler.DrpcConsumerInvocationHandler;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Proxy;

@Slf4j
public class ReferenceConfig<T> {

    private Class<T> interfaceConsumer;

    private Register register;


    // 代理设计模式，生成一个api接口的代理对象
    public T get() {
        // 动态代理
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceConsumer};
        DrpcConsumerInvocationHandler handler = new DrpcConsumerInvocationHandler(register, interfaceConsumer);

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes,handler);

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

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }




}
