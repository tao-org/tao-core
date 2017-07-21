package ro.cs.tao.datasource.remote.aws;

import ro.cs.tao.datasource.common.DataQuery;
import ro.cs.tao.datasource.common.DataSource;
import ro.cs.tao.datasource.common.ParameterProvider;
import ro.cs.tao.datasource.common.QueryException;
import ro.cs.tao.eodata.EOData;

import java.util.List;

/**
 * @author Cosmin Cara
 */
class Sentinel2Query extends DataQuery<EOData> {

    public Sentinel2Query(DataSource source, ParameterProvider parameterProvider) {
        super(source, parameterProvider);
    }

    @Override
    protected List<EOData> executeImpl() throws QueryException {
        return null;
    }
}
