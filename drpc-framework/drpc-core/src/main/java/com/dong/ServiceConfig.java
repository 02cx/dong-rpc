package com.dong;


public class ServiceConfig<T> {

    private Class<?> interfaceProvider;

    private Object ref;

    public ServiceConfig(Class<T> interfaceProvider, Object ref) {
        this.interfaceProvider = interfaceProvider;
        this.ref = ref;
    }

    public ServiceConfig() {
    }

    public Class<?> getInterface() {
        return interfaceProvider;
    }

    public void setInterface(Class<?> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
