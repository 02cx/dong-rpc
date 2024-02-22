package com.dong;


public class ServiceConfig<T> {

    private Class<T> interfaceProvider;

    private Object ref;

    public ServiceConfig(Class<T> interfaceProvider, Object ref) {
        this.interfaceProvider = interfaceProvider;
        this.ref = ref;
    }

    public ServiceConfig() {
    }

    public Class<T> getInterface() {
        return interfaceProvider;
    }

    public void setInterface(Class<T> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
