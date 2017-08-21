package ro.cs.tao.datasource.remote.scihub.helpers;

/**
 * @author Cosmin Cara
 */
public enum ProductType {
    L1C("S2MSI1C"),
    L2A("S2MSI2Ap");

    private String value;
    ProductType(String value) { this.value = value; }

    @Override
    public String toString() {
        return this.value;
    }
}
