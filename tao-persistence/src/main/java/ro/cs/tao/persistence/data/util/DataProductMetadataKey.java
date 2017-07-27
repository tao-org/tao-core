package ro.cs.tao.persistence.data.util;

import ro.cs.tao.persistence.data.DataProduct;

import java.io.Serializable;

/**
 * Created by oana on 7/26/2017.
 */
public class DataProductMetadataKey implements Serializable {
    private DataProduct dataProduct;
    private String attributeName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null|| getClass() != o.getClass()) return false;

        DataProductMetadataKey that = (DataProductMetadataKey) o;

        if (dataProduct != null ?
          !dataProduct.equals(that.dataProduct) : that.dataProduct !=null)
            return false;
        if (attributeName != null ?
          !attributeName.equals(that.attributeName) : that.attributeName !=null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (dataProduct != null ? dataProduct.hashCode() : 0);
        result =31* result + (attributeName != null ? attributeName.hashCode() : 0);
        return result;
    }
}
