package ro.cs.tao.services.model.monitoring;

import java.util.Arrays;

/**
 * @author Cosmin Cara
 */
public enum MemoryUnit {
    BYTE(1),
    KILOBYTE(1024),
    MEGABYTE(1024*1024),
    GIGABYTE(1024*1024*1024);

    private final int value;

    MemoryUnit(int value) { this.value = value; }
    @Override
    public String toString() { return String.valueOf(this.value); }

    public int value() { return this.value; }

    public static String getEnumConstantNameByValue(final int value) {
        return Arrays.stream(values()).filter(t -> String.valueOf(value).equals(t.toString())).findFirst().get().name();
    }

}
