package ro.cs.tao.utils;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicDouble extends Number {

    private final AtomicLong longValue;

    public AtomicDouble() {
        this(0L);
    }

    public AtomicDouble(double value) {
        this.longValue = new AtomicLong(Double.doubleToLongBits(value));
    }

    public void set(double value) {
        longValue.set(Double.doubleToLongBits(value));
    }

    public double get() {
        return Double.longBitsToDouble(longValue.get());
    }

    public double addAndGet(double delta) {
        return Double.longBitsToDouble(longValue.addAndGet(Double.doubleToLongBits(delta)));
    }

    public double getAndAdd(double delta) {
        return Double.longBitsToDouble(longValue.getAndAdd(Double.doubleToLongBits(delta)));
    }

    @Override
    public int intValue() {
        return (int) get();
    }

    @Override
    public long longValue() {
        return longValue.get();
    }

    @Override
    public float floatValue() {
        return (float) get();
    }

    @Override
    public double doubleValue() {
        return get();
    }
}
