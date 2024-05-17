package liuyao.utils;

public class SingleSnowFlakeId extends SnowFlakeId {

    public SingleSnowFlakeId(long datacenterId, long machineId) {
        super(datacenterId, machineId);
    }

    @Override
    protected long curTimeGen() {
        return System.currentTimeMillis();
    }
}
