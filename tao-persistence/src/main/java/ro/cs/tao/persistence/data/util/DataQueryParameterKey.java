package ro.cs.tao.persistence.data.util;

import ro.cs.tao.persistence.data.DataQuery;
import ro.cs.tao.persistence.data.QueryParameter;

import java.io.Serializable;

/**
 * Created by oana on 7/26/2017.
 */
public class DataQueryParameterKey implements Serializable {
    private DataQuery dataQuery;
    private QueryParameter queryParameter;
}
