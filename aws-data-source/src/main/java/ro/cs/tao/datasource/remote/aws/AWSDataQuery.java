package ro.cs.tao.datasource.remote.aws;

import ro.cs.tao.datasource.common.DataQuery;
import ro.cs.tao.datasource.common.ParameterProvider;
import ro.cs.tao.datasource.common.QueryException;
import ro.cs.tao.datasource.remote.aws.parameters.LandsatParameterProvider;
import ro.cs.tao.datasource.remote.aws.parameters.Sentinel2ParameterProvider;
import ro.cs.tao.eodata.EOData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class AWSDataQuery extends DataQuery<EOData> {
    private DataQuery<EOData> innerQuery;

    AWSDataQuery(AWSDataSource source, ParameterProvider parameterProvider) {
        super(source, parameterProvider);
        if (parameterProvider != null) {
            if (parameterProvider instanceof Sentinel2ParameterProvider) {
                innerQuery = new Sentinel2Query(source, parameterProvider);
            } else if (parameterProvider instanceof LandsatParameterProvider) {
                innerQuery = new Landsat8Query(source, parameterProvider);
            }
        }
    }

    @Override
    protected List<EOData> executeImpl() throws QueryException {
        return innerQuery != null ? innerQuery.execute() : new ArrayList<>();
    }
}
