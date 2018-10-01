package ro.cs.tao.datasource.remote;

import ro.cs.tao.TaoEnum;

public enum ProductFormat implements TaoEnum<Integer> {
    FOLDER(1, ""),
    ZIP(2, ".zip"),
    TAR_GZ(3, ".tar.gz");

    private final int value;
    private final String description;

    ProductFormat(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return description; }

    @Override
    public Integer value() { return value; }
}
