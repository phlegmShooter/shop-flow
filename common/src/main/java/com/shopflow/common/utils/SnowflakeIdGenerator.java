package com.shopflow.common.utils;

import org.springframework.stereotype.Component;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;

/**
 * 雪花ID生成器
 * 用于生成分布式系统唯一ID
 * 结构：1位符号位 + 41位时间戳 + 10位机器ID + 12位序列号
 */
@Component
public class SnowflakeIdGenerator {

    // 起始时间戳（2026-03-25 00:00:00）
    private static final long START_TIMESTAMP = 1742860800000L;

    // 机器ID位数
    private static final long WORKER_ID_BITS = 5L;

    // 数据中心ID位数
    private static final long DATACENTER_ID_BITS = 5L;

    // 最大机器ID（2^5 - 1 = 31）
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    // 最大数据中心ID（2^5 - 1 = 31）
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    // 序列号位数
    private static final long SEQUENCE_BITS = 12L;

    // 机器ID左移位数
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    // 数据中心ID左移位数
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    // 时间戳左移位数
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    // 序列号掩码（2^12 - 1 = 4095）
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    // 机器ID（0-31）
    private final long workerId;

    // 数据中心ID（0-31）
    private final long datacenterId;

    // 序列号（0-4095）
    private long sequence = 0L;

    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    /**
     * 构造函数，使用默认的机器ID和数据中心ID
     */
    public SnowflakeIdGenerator() {
        this.datacenterId = getDatacenterId();
        this.workerId = getWorkerId(datacenterId);
    }

    /**
     * 构造函数，指定机器ID和数据中心ID
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                String.format("Datacenter ID must be between 0 and %d", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成下一个ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上次生成ID的时间戳，说明系统时钟回退，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        // 如果是同一毫秒内生成的，则序列号加1
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 序列号溢出（同一毫秒内生成了4096个ID），等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 新的毫秒，序列号从0开始
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组合各部分生成ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 生成订单号（基于雪花ID，格式：ORDER + 雪花ID）
     */
    public String generateOrderNo() {
        return "ORDER" + nextId();
    }

    /**
     * 等待下一毫秒
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    private long timeGen() {
        return Instant.now().toEpochMilli();
    }

    /**
     * 获取数据中心ID（基于MAC地址）
     */
    private long getDatacenterId() {
        long datacenterId;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                }
            }
            datacenterId = sb.toString().hashCode();
        } catch (Exception e) {
            // 如果获取MAC地址失败，使用随机数
            datacenterId = new SecureRandom().nextInt(31);
        }
        return datacenterId & MAX_DATACENTER_ID;
    }

    /**
     * 获取机器ID（基于进程ID）
     */
    private long getWorkerId(long datacenterId) {
        String processId = System.getenv("PROCESS_ID");
        if (processId != null) {
            return (Long.parseLong(processId) & MAX_WORKER_ID);
        }

        // 如果没有设置进程ID，使用随机数
        SecureRandom secureRandom = new SecureRandom();
        long workerId = secureRandom.nextInt(31);

        // 确保同一数据中心内的机器ID不重复
        return (workerId + datacenterId) & MAX_WORKER_ID;
    }

    /**
     * 解析雪花ID
     */
    public static SnowflakeId parseId(long id) {
        long timestamp = (id >> TIMESTAMP_LEFT_SHIFT) + START_TIMESTAMP;
        long datacenterId = (id >> DATACENTER_ID_SHIFT) & MAX_DATACENTER_ID;
        long workerId = (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
        long sequence = id & SEQUENCE_MASK;

        return new SnowflakeId(timestamp, datacenterId, workerId, sequence);
    }

    /**
     * 雪花ID解析结果
     */
    public static class SnowflakeId {
        private final long timestamp;
        private final long datacenterId;
        private final long workerId;
        private final long sequence;

        public SnowflakeId(long timestamp, long datacenterId, long workerId, long sequence) {
            this.timestamp = timestamp;
            this.datacenterId = datacenterId;
            this.workerId = workerId;
            this.sequence = sequence;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getDatacenterId() {
            return datacenterId;
        }

        public long getWorkerId() {
            return workerId;
        }

        public long getSequence() {
            return sequence;
        }

        @Override
        public String toString() {
            return String.format("SnowflakeId{timestamp=%d, datacenterId=%d, workerId=%d, sequence=%d}",
                timestamp, datacenterId, workerId, sequence);
        }
    }
}