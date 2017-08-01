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

    @Override
    public boolean equals(Object o) {
        if (this == o)
        {
            return true;
        }
        if (o == null|| getClass() != o.getClass())
        {
            return false;
        }

        DataQueryParameterKey that = (DataQueryParameterKey) o;

        if (dataQuery != null ?
          !dataQuery.equals(that.dataQuery) : that.dataQuery !=null)
            return false;
        if (queryParameter != null ?
          !queryParameter.equals(that.queryParameter) : that.queryParameter !=null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (dataQuery != null ? dataQuery.hashCode() : 0);
        result = 31 * result + (queryParameter != null ? queryParameter.hashCode() : 0);
        return result;
    }
}
