package liuyao.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 雪花算法
 * 64位 = 1（符号位 0） + 41（时间戳 可用69年） + 5（数据中心、区域 id） + 5（当前数据中心机器id） + 12（自增序列）
 */
public abstract class SnowFlakeId {
    // 起始的时间戳
    protected final static long START_STMP = 1646064000000L; // 2022-03-01 00:00:00
    // 每一部分占用的位数，就三个
    protected final static long SEQUENCE_BIT = 12; //序列号占用的位数
    protected final static long MACHINE_BIT = 5; //机器标识占用的位数
    protected final static long DATACENTER_BIT = 5; //数据中心占用的位数
    // 每一部分最大值
    protected final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    protected final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    protected final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);
    // 每一部分向左的位移
    protected final static long MACHINE_LEFT = SEQUENCE_BIT;
    protected final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    protected final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    protected final long datacenterId; //数据中心
    protected final long machineId; //机器标识
    protected volatile long sequence = 0L; //序列号
    protected volatile long lastStmp; //上一次时间戳

    protected final Lock lock = new ReentrantLock();

    public SnowFlakeId(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    public final void init() {
        this.lastStmp = curTimeGen();
    }

    public final long nextId() {
        lock.lock();
        long curTime = curTimeGen();
        if (curTime < lastStmp) {
            lock.unlock();
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        } else if (curTime > lastStmp) {
            lastStmp = curTime;
            sequence = 0L;
        } else {
            long seq = ++sequence;
            if (seq > MAX_SEQUENCE) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                curTime = curTimeGen();
                lastStmp = curTime;
                sequence = 0;
            }
        }
        long seq = sequence;
        lock.unlock();
        return calcId(curTime, seq);
    }

    protected abstract long curTimeGen();

    protected final long calcId(long time, long seq)  {
        return (time - START_STMP) << TIMESTMP_LEFT //时间戳部分
                | datacenterId << DATACENTER_LEFT       //数据中心部分
                | machineId << MACHINE_LEFT             //机器标识部分
                | seq;                             //序列号部分
    }

}