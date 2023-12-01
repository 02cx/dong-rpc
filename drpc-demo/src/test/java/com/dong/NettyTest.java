package com.dong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NettyTest {

    @Test
    public void testByteBuf(){
        ByteBuf byteBuf = Unpooled.buffer();
    }

    @Test
    public void testCompress() throws IOException {
        // 压缩buf
        byte[] buf = new byte[]{12,14,15,21,14,15,67,12,14,15,21,14,15,67,12,14,15,21,14,15,67,12,14,15,21,14,15,67,12,14,15,21,14,15,67,12,14,15,21,14,15,67};
        // 使用GZIP进行压缩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(baos);

        gos.write(buf);
        gos.finish();

        byte[] bytes = baos.toByteArray();
        System.out.println(buf.length + "----->" + bytes.length);
        System.out.println("压缩后：" + Arrays.toString(bytes));
    }

    @Test
    public void deCompress() throws IOException {
        // 解压缩
        byte[] bytes = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -29, -31, -29, 23, -27, -29, 119, -26, 33, 76, 1, 0, -62, -81, -90, 104, 42, 0, 0, 0};
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        GZIPInputStream gis = new GZIPInputStream(bais);
        byte[] buf = gis.readAllBytes();
        System.out.println(bytes.length + "---->" + buf.length);
        System.out.println(Arrays.toString(buf));
    }
}
