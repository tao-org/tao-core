package ro.cs.tao.persistence.data.util;

import ro.cs.tao.persistence.data.DataProduct;

import java.io.Serializable;

/**
 * Created by oana on 7/26/2017.
 */
public class DataProductMetadataKey implements Serializable {
    private DataProduct dataProduct;
    private String attributeName;
}
