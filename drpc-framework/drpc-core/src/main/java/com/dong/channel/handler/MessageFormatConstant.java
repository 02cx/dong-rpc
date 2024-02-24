package com.dong.channel.handler;

public class MessageFormatConstant {

    public static final byte[] MAGIC = "drpc".getBytes();
    public static final byte VERSION = 1;
    public static final short HEADER_LENGTH = (short) (MAGIC.length + 19);
}
