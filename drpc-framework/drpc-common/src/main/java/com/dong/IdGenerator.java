package com.dong;

import com.dong.utils.DateUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.LongAdder;

/**
 * 通信id生成器
 */
public class IdGenerator {

    // 起始时间戳
    public static final long START_STAMP = DateUtil.get("2020-09-01 01:00:00").toEpochSecond(ZoneOffset.ofHours(8));
    // 机房号  机器号  序列号  占位bit
    public static final long DATA_CENTER_BIT = 5L;
    public static final long MACHINE_BIT = 5L;
    public static final long SEQUENCE_BIT = 12L;

    // 最大值
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    // 时间戳（42） 机房号（5）  机器号（5） 序列号（12）
    // 101010101010101010101010101010101010101011 10101 10101 1010101010
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    public static final long DATA_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;


    private Long dataCenterId;
    private Long machineId;
    private LongAdder sequenceId = new LongAdder();
    // 时钟回拨
    private Long lastTimeStamp = -1L;

    public IdGenerator(Long dataCenterId, Long machineId) {
        if(dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX){
            throw new IllegalArgumentException("传入的数据中心编号或机器号不合法");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId(){
        // 1.处理时间戳
        long currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
        long timeStamp = currentTime - START_STAMP;
        // 判断始终回拨
        if(timeStamp < lastTimeStamp){
            throw new RuntimeException("您的服务器进行了时钟回调");
        }

        // 2.处理序列号，如果是同一个时间节点，sequenceId必须自增
        if(timeStamp == lastTimeStamp){
            sequenceId.increment();
            if(sequenceId.sum() >= SEQUENCE_MAX){
                timeStamp = getNextTimeStamp();
            }
        }else{
            sequenceId.reset();
        }

        // 记录上一次的时间戳
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.longValue();
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_LEFT | machineId << MACHINE_LEFT | sequence;
    }

    /**
     * 获取下一个时间戳
     * @return
     */
    private long getNextTimeStamp() {
        long current = System.currentTimeMillis() - START_STAMP;
        while(current == lastTimeStamp){
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }


    public static void main(String[] args) {


        IdGenerator idGenerator = new IdGenerator(1L, 2L);
        for (int i = 0; i < 1000; i++) {
            new Thread(()->{
                System.out.println(idGenerator.getId());
            }).start();
        }

    }
}














