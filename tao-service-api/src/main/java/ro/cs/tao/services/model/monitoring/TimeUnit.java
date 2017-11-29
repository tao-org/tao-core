package ro.cs.tao.services.model.monitoring;

import java.util.Arrays;

/**
 * @author Cosmin Cara
 */
public enum TimeUnit {
    MILLISECONDS(1),
    SECONDS(1000),
    MINUTES(60000);

    private final int value;

    TimeUnit(int value) { this.value = value; }
    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }

    public int value() { return this.value; }

    public static String getEnumConstantNameByValue(final int value) {
        return Arrays.stream(values()).filter(t -> String.valueOf(value).equals(t.toString())).findFirst().get().name();
    }
}
