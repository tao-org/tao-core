package ro.cs.tao.datasource.param;

public class WKT {
    private final String value;

    public WKT(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
