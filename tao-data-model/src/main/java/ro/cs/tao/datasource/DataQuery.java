/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.datasource;

import org.locationtech.jts.geom.Geometry;
import ro.cs.tao.component.ParameterDependency;
import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.component.enums.DependencyType;
import ro.cs.tao.datasource.converters.ConversionException;
import ro.cs.tao.datasource.converters.ConverterFactory;
import ro.cs.tao.datasource.param.CommonParameterNames;
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.serialization.*;
import ro.cs.tao.utils.StringUtilities;

import javax.xml.bind.annotation.XmlTransient;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Abstraction for a datasource query.
 *
 * @author Cosmin Cara
 */
@XmlTransient
public abstract class DataQuery extends StringIdentifiable {
    protected static final int DEFAULT_LIMIT = Integer.MAX_VALUE;
    protected static final int DEFAULT_PAGE_SIZE = 50;
    protected static final String QUERY_RESULT_MESSAGE = "Query %s [%s-%s] (page #%d) returned %d results";
    protected static final Map<Class<? extends DataQuery>, ConverterFactory> converterFactory = new HashMap<>();
    /* Correspondence between system parameter names and remote parameter names */
    private Map<String, String> parameterNameMap;
    /* Set of mandatory parameter system names */
    private Set<String> mandatoryParams;
    protected DataSource<?, ?> source;
    protected String sensorName;
    private String queryText;
    protected int pageSize;
    protected int pageNumber;
    protected int limit;
    protected double coverage;
    protected long timeout;
    /* Introduces a delay between consecutive queries, expressed in seconds */
    protected int queryDelay;
    /* Maps parameter system name to a data source parameter (which has the remote name) */
    protected Map<String, DataSourceParameter> dataSourceParameters;
    /* Maps parameter system name to a query parameter (which has the same system name) */
    protected Map<String, QueryParameter<?>> parameters;
    protected Predicate<EOProduct> coverageFilter;

    protected Logger logger = Logger.getLogger(getClass().getName());

    protected DataQuery() { }

    public DataQuery(DataSource<?, ?> source, String sensorName) {
        if (source == null) {
            throw new QueryException("Empty source");
        }
        if (sensorName == null || Arrays.stream(source.getSupportedSensors()).noneMatch(sensorName::equals)) {
            throw new QueryException("Empty sensor name");
        }
        initialize(source, sensorName);
    }

    public DataSource<?, ?> getSource() {
        return source;
    }

    public void setSource(DataSource<?, ?> source) {
        this.source = source;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public int getQueryDelay() { return queryDelay; }

    public void setQueryDelay(int queryDelay) { this.queryDelay = queryDelay; }

    public QueryParameter<?> addParameter(QueryParameter<?> parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("Cannot accept null parameter");
        }
        checkSupported(parameter.getName(), parameter.getType());
        this.parameters.put(parameter.getName(), parameter);
        return parameter;
    }

    public <V> QueryParameter<V> addParameter(String name, Class<V> type) {
        QueryParameter<V> parameter = createParameter(name, type);
        this.parameters.put(name, parameter);
        return parameter;
    }

    public <V> QueryParameter<V> addParameter(String name, Class<V> type, V value) {
        QueryParameter<V> parameter = createParameter(name, type, value);
        this.parameters.put(name, parameter);
        return parameter;
    }

    private <V> QueryParameter<V> replaceParameter(String name, Class<V> type, V value) {
        QueryParameter<V> parameter = createParameter(name, type, value);
        this.parameters.replace(name, parameter);
        return parameter;
    }

    public <V> QueryParameter<V> addParameter(String name, V value) {
        Class<V> clazz = this.dataSourceParameters.get(name).getType();
        QueryParameter<V> parameter = createParameter(name, clazz, value);
        this.parameters.put(name, parameter);
        return parameter;
    }

    public QueryParameter<?> getParameter(String name) { return this.parameters.get(name); }

    public Set<String> getMandatoryParams() { return this.mandatoryParams; }

    public int getParameterCount() { return this.parameters.size(); }

    public String getText() { return this.queryText; }

    public void setText(String value) { this.queryText = value; }

    public void setPageSize(int value) { this.pageSize = value; }

    public void setPageNumber(int value) { this.pageNumber = value; }

    public void setMaxResults(int value) { this.limit = value; }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }

    public boolean supportsPaging() { return true; }

    public List<EOProduct> execute() {
        final Set<String> mandatoryParams = getMandatoryParams();
        final Map<String, QueryParameter<?>> actualParameters = getParameters();
        final List<String> errors = mandatoryParams.stream()
                                                   .filter(p -> !actualParameters.containsKey(p)).collect(Collectors.toList());
        if (!errors.isEmpty()) {
            QueryException ex = new QueryException("Mandatory parameter(s) not supplied");
            ex.addAdditionalInfo("Missing", String.join(",", errors));
            throw ex;
        }
        errors.addAll(checkDependencies(actualParameters));
        if (!errors.isEmpty()) {
            QueryException ex = new QueryException("Some parameter(s) dependencies are not satisfied");
            ex.addAdditionalInfo("Dependencies", String.join(",", errors));
            throw ex;
        }
        Instant start = Instant.now();
        final QueryParameter<?> footprintParam = actualParameters.get(CommonParameterNames.FOOTPRINT);
        if (this.coverage > 0.0 && footprintParam != null) {
            final Polygon2D polygon2D = (Polygon2D) footprintParam.getValue();
            final Geometry footprint;
            try {
                footprint = new GeometryAdapter().marshal(polygon2D.toWKT());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            final double footprintArea = footprint.getArea();
            this.coverageFilter = new Predicate<EOProduct>() {
                @Override
                public boolean test(EOProduct p) {
                    // If coverage parameter is set, then keep only those products
                    // that intersect at least <coverage>% (of the given footprint) the area of interest
                    try {
                        Geometry product = p.geometryObject();
                        final Geometry intersection = footprint.intersection(product);
                        if (intersection.isEmpty()) {
                            return true;
                        }
                        final double productArea = product.getArea();
                        final double areaPercentage = productArea > footprintArea
                                                      ? intersection.getArea() / footprintArea
                                                      : intersection.getArea() / productArea;
                        final boolean toRemove = areaPercentage < coverage;
                        if (toRemove) {
                            logger.finest(String.format("Product %s removed due to coverage (%.2f%%)", p.getName(), areaPercentage));
                        }
                        return toRemove;
                    } catch (Exception e) {
                        logger.warning(e.getMessage());
                        return true;
                    }
                }
            };
        }
        List<EOProduct> retList = executeImpl();
        logger.finest(String.format("Data query completed in %d ms.", Duration.between(start, Instant.now()).toMillis()));

        boolean canSortAcqDate = true;
        for (EOProduct p : retList) {
            if (p.getVisibility() == null) {
                p.setVisibility(Visibility.PUBLIC);
            }
            canSortAcqDate &= p.getAcquisitionDate() != null;
        }
        if (canSortAcqDate) {
            retList.sort(Comparator.comparing(EOProduct::getAcquisitionDate));
        }
        return retList;
    }

    public long getCount() {
        final Set<String> mandatoryParams = getMandatoryParams();
        final Map<String, QueryParameter<?>> parameters = getParameters();
        List<String> missing = mandatoryParams.stream()
                .filter(p -> !parameters.containsKey(p)).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            final String list = String.join(",", missing);
            QueryException ex = new QueryException("Mandatory parameters not supplied: " + list);
            ex.addAdditionalInfo("Missing", list);
            throw ex;
        }
        for (Map.Entry<String, DataSourceParameter> entry : this.dataSourceParameters.entrySet()) {
            final DataSourceParameter parameter = entry.getValue();
            if (parameter.getDefaultValue() != null) {
                if (!parameters.containsKey(entry.getKey())) {
                    addParameter(entry.getKey(), parameter.getType(), parameter.getDefaultValue());
                } else {
                    QueryParameter<?> queryParameter = parameters.get(entry.getKey());
                    if (queryParameter.getType().equals(String.class) &&
                            (queryParameter.getValue() == null || ((String) queryParameter.getValue()).trim().isEmpty()) && !((String) parameter.getDefaultValue()).trim().isEmpty()) {
                        replaceParameter(entry.getKey(), parameter.getType(), parameter.getDefaultValue());
                    }
                }
            }
        }
        Instant start = Instant.now();
        final long count = getCountImpl();
        logger.finest(String.format("Count query completed in %d ms.", Duration.between(start, Instant.now()).toMillis()));
        return count;
    }

    public Map<String, DataSourceParameter> getSupportedParameters() { return this.dataSourceParameters; }

    public Map<String, QueryParameter<?>> getParameters() { return this.parameters; }

    public <V> QueryParameter<V> createParameter(String name, Class<V> type) {
        checkSupported(name, type);
        return new QueryParameter<>(type, name);
    }

    public <V> QueryParameter<V> createParameter(String name, Class<V> type, V value) {
        checkSupported(name, type);
        final QueryParameter<V> parameter = new QueryParameter<>(type, name, value);
        checkValue(parameter, value);
        return parameter;
    }

    public <V> QueryParameter<V> createParameter(String name, Class<V> type, V value, boolean optional) {
        checkSupported(name, type);
        final QueryParameter<V> parameter = new QueryParameter<>(type, name, value, optional);
        checkValue(parameter, value);
        return parameter;
    }

    public <V> QueryParameter<V> createParameter(String name, Class<V> type, V minValue, V maxValue) {
        checkSupported(name, type);
        return new QueryParameter<>(type, name, minValue, maxValue);
    }

    public <V> QueryParameter<V> createParameter(String name, Class<V> type, V minValue, V maxValue, boolean optional) {
        checkSupported(name, type);
        return new QueryParameter<>(type, name, minValue, maxValue, optional);
    }

    @Override
    public DataQuery clone() throws CloneNotSupportedException {
        DataQuery newQuery = (DataQuery) super.clone();
        newQuery.initialize(this.source, this.sensorName);
        newQuery.parameters.putAll(this.parameters);
        return newQuery;
    }

    public String exportParametersAsXML() throws SerializationException {
        final BaseSerializer<QueryParameter> serializer = SerializerFactory.create(QueryParameter.class, MediaType.XML);
        return serializer.serialize(new ArrayList<>(this.parameters.values()), "parameters");
    }

    public void importParameters(String xml) throws SerializationException {
        final BaseSerializer<QueryParameter> serializer = SerializerFactory.create(QueryParameter.class, MediaType.XML);
        List<QueryParameter> parameters = serializer.deserialize(QueryParameter.class, xml);
        this.parameters.clear();
        for (QueryParameter parameter : parameters) {
            addParameter(parameter.getName(), parameter.getValue());
        }
    }

    protected void checkSupported(String name, Class<?> type) {
        DataSourceParameter descriptor = this.dataSourceParameters.get(name);
        if (descriptor == null) {
            throw new QueryException(String.format("Parameter [%s] not supported on this data source", name));
        }
        if (!descriptor.getType().isAssignableFrom(type)) {
            throw new QueryException(
                    String.format("Wrong type for parameter [%s]: expected %s, found %s",
                                  name, descriptor.getType().getSimpleName(), type.getSimpleName()));
        }
        if (this.parameters.containsKey(name)) {
            throw new QueryException(
                    String.format("Parameter [%s] already exists", name));
        }
    }

    protected <V> void checkValue(QueryParameter<V> parameter, V value) {
        DataSourceParameter descriptor = this.dataSourceParameters.get(parameter.getName());
        final Object[] valueSet = descriptor.getValueSet();
        if (valueSet != null && value != null) {
            final String stringValue = value.toString();
            List<String> strValues = Arrays.stream(valueSet).map(Object::toString).collect(Collectors.toList());
            if (strValues.stream().noneMatch(v -> v.equals(stringValue))) {
                throw new QueryException(String.format("Unsupported value for parameter [%s]: expected one of %s but found %s",
                                                       parameter.getName(), String.join(",", strValues),
                                                       StringUtilities.isNullOrEmpty(stringValue) ? "''" : stringValue));
            }
        }
    }

    protected abstract List<EOProduct> executeImpl();

    protected long getCountImpl() { return -1; }

    protected boolean supports(String systemName) {
        return this.parameterNameMap.containsKey(systemName);
    }

    protected String getRemoteName(String systemName) {
        if (this.parameterNameMap.containsKey(systemName)) {
            return this.parameterNameMap.get(systemName);
        } else {
            throw new QueryException(String.format("Parameter [%s] not supported on this data source", systemName));
        }
    }

    protected String getParameterValue(QueryParameter<?> parameter) throws ConversionException {
        return getRemoteMappedValue(parameter.getName(),
                                    parameter.getType().isArray() && LocalDateTime.class.equals(parameter.getType().getComponentType()) ?
                                            converterFactory.get(getClass()).simple((QueryParameter<LocalDateTime[]>) parameter).stringValue() :
                                            converterFactory.get(getClass()).create(parameter).stringValue());
    }

    protected String getRemoteMappedValue(String parameterName, String mappedValue) {
        final Map<String, Map<String, Map<String, String>>> filteredParameters = this.source.getFilteredParameters();
        String value = mappedValue;
        boolean found = false;
        if (filteredParameters != null) {
            final Map<String, Map<String, String>> valueMap = filteredParameters.values().stream().filter(v -> v.containsKey(parameterName)).findFirst().orElse(null);
            if (valueMap != null) {
                for (Map<String, String> map : valueMap.values()) {
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        if (mappedValue.equals(entry.getValue())) {
                            value = entry.getKey();
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
        }
        return value;
    }

    protected void sleep() {
        if (this.queryDelay > 0) {
            try {
                Thread.sleep(this.queryDelay * 1000L);
            } catch (java.lang.InterruptedException ignored) {
            }
        }
    }

    protected List<String> checkDependencies(Map<String, QueryParameter<?>> actualParameters) {
        final List<String> errors = new ArrayList<>();
        for (Map.Entry<String, DataSourceParameter> entry : this.dataSourceParameters.entrySet()) {
            final DataSourceParameter dataSourceParameter = entry.getValue();
            if (!actualParameters.containsKey(entry.getKey())) {
                // Parameters not supplied that have defined a default value will be added with the default value
                if (dataSourceParameter.getDefaultValue() != null) {
                    addParameter(entry.getKey(), dataSourceParameter.getType(), dataSourceParameter.getDefaultValue());
                }
            } else {
                // Check the supplied parameters for satisfying any defined dependency
                final List<ParameterDependency> dependencies = dataSourceParameter.getDependencies();
                if (dependencies != null) {
                    //Map<String, ParameterDependency> deps = dependencies.stream().collect(Collectors.toMap(ParameterDependency::getExpectedValue, Function.identity()));
                    for (ParameterDependency dependency : dependencies) {
                        QueryParameter<?> refParam = actualParameters.get(dependency.getReferencedParameterId());
                        if (refParam == null && dependencies.stream().noneMatch(d -> d.getDependencyType() == DependencyType.REQUIRED_IF)) {
                            errors.add(entry.getKey() + " cannot be set if " + dependency.getReferencedParameterId() + " is not set");
                            continue;
                        }
                        if (refParam == null) {
                            refParam = parameters.get(dependency.getReferencedParameterId());
                        }
                        QueryParameter<?> currentParam = actualParameters.get(entry.getKey());
                        boolean hasError = false;
                        switch (dependency.getDependencyType()) {
                            case EXCLUSIVE:
                                errors.add(entry.getKey() + " cannot be set if " + dependency.getReferencedParameterId() + " is not set");
                                break;
                            case FILTER:
                                final String expectedValue = dependency.getExpectedValue();
                                if (refParam.getValueAsString().equals(expectedValue)) {
                                    String allowedValues = dependency.getAllowedValues();
                                    final Set<String> aValues = allowedValues != null ? Arrays.stream(allowedValues.split(",")).collect(Collectors.toSet()) : new HashSet<>();
                                    final Set<String> expValues = Arrays.stream(expectedValue.split(",")).collect(Collectors.toSet());
                                    switch (dependency.getCondition()) {
                                        case EQ:
                                            hasError = !expectedValue.equals(refParam.getValueAsString());
                                            break;
                                        case NEQ:
                                            hasError = expectedValue.equals(refParam.getValueAsString());
                                            break;
                                        case IN:
                                            hasError = !expValues.contains(refParam.getValueAsString());
                                            break;
                                        case NOTIN:
                                            hasError = expValues.contains(refParam.getValueAsString());
                                            break;
                                        case GT:
                                            hasError = compare(refParam.getType(), expectedValue, refParam.getValue()) <= 0;
                                            break;
                                        case GTE:
                                            hasError = compare(refParam.getType(), expectedValue, refParam.getValue()) < 0;
                                            break;
                                        case LT:
                                            hasError = compare(refParam.getType(), expectedValue, refParam.getValue()) >= 0;
                                            break;
                                        case LTE:
                                            hasError = compare(refParam.getType(), expectedValue, refParam.getValue()) > 0;
                                            break;
                                    }
                                    if (hasError) {
                                        errors.add(String.format("%s cannot be set to %s if %s %s %s",
                                                                 entry.getKey(), currentParam.getValueAsString(),
                                                                 refParam.getName(), dependency.getCondition().friendlyName().toLowerCase(), refParam.getValue()));
                                    }
                                }
                                /*if (!aValues.contains(currentParam.getValueAsString())) {
                                    errors.add(String.format("Value %s of %s not allowed when %s %s %s",
                                            currentParam.getValueAsString(), entry.getKey(),
                                            refParam.getName(), dependency.getCondition().friendlyName().toLowerCase(), refParam.getValue()));
                                }*/
                                break;
                            case REQUIRED_IF:
                                switch (dependency.getCondition()) {
                                    case NOTSET:
                                        hasError = StringUtilities.isNullOrEmpty(currentParam.getValueAsString()) &&
                                                   refParam != null && StringUtilities.isNullOrEmpty(refParam.getValueAsString());
                                        /*errors.add(String.format("%s must be set if %s is not set",
                                                                 entry.getKey(), dependency.getReferencedParameterId()));*/
                                        break;
                                    case SET:
                                        hasError = StringUtilities.isNullOrEmpty(currentParam.getValueAsString()) &&
                                                   (refParam == null || !StringUtilities.isNullOrEmpty(refParam.getValueAsString()));
                                        /*errors.add(String.format("%s must be set if %s is set",
                                                                 entry.getKey(), dependency.getReferencedParameterId()));*/
                                        break;
                                }
                                if (hasError) {
                                    errors.add(String.format("%s cannot be set to %s if %s %s %s",
                                                             entry.getKey(), currentParam.getValueAsString(),
                                                             dependency.getReferencedParameterId(),
                                                             dependency.getCondition().friendlyName().toLowerCase(),
                                                             refParam != null ? refParam.getValue() : "null"));
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        return errors;
    }

    private void initialize(DataSource<?, ?> source, String sensorName) {
        this.source = source;
        this.sensorName = sensorName;
        this.parameters = new LinkedHashMap<>();
        this.timeout = 10000;

        this.pageSize = -1;
        this.pageNumber = -1;
        this.limit = -1;
        Map<String, Map<String, DataSourceParameter>> supportedParameters = source.getSupportedParameters();
        this.dataSourceParameters = supportedParameters.get(sensorName);
        this.parameterNameMap = this.dataSourceParameters.values().stream()
                .collect(Collectors.toMap(DataSourceParameter::getName, DataSourceParameter::getRemoteName));
        this.mandatoryParams = this.dataSourceParameters.entrySet().stream()
                .filter(e -> e.getValue().isRequired() && e.getValue().getDefaultValue() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private int compare(Class<?> type, String val1, Object val2) {
        if (type.equals(byte.class) || type.equals(Byte.class)) {
            return Byte.compare(Byte.parseByte(val1), (byte) val2);
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            return Short.compare(Short.parseShort(val1), (short) val2);
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.compare(Integer.parseInt(val1), (int) val2);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return Long.compare(Long.parseLong(val1), (long) val2);
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            return Float.compare(Float.parseFloat(val1), (float) val2);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.compare(Double.parseDouble(val1), (double) val2);
        } else {
            return val1.compareTo(val2.toString());
        }
    }
}
