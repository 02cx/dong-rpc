package com.dong;

import java.lang.invoke.CallSite;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReferenceConfig<T> {

    private Class<T> interfaceConsumer;


    public T get() {
        // 动态代理
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceConsumer};
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("hello proxy");
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


}
