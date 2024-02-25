package com.dong.enumeration;

public enum RequestType {

    REQUEST((byte)1,"普通请求"),HEAD_BEAT((byte)2,"心跳检查请求");

    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    public byte getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
