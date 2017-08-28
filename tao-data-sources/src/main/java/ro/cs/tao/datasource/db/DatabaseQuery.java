/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

package ro.cs.tao.datasource.db;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import ro.cs.tao.component.Identifiable;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.converters.ConverterFactory;
import ro.cs.tao.datasource.converters.DateConverter;
import ro.cs.tao.datasource.converters.PolygonConverter;
import ro.cs.tao.datasource.param.ParameterProvider;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.eodata.serialization.GeometryAdapter;

import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class DatabaseQuery extends DataQuery<EOData> {

    private static final String PRODUCTS_TABLE = "Products";
    private static final String PRODUCT_PARAMS_TABLE = "ProductsParams";
    private static final ConverterFactory converterFactory = ConverterFactory.getInstance();

    static {
        converterFactory.register(PolygonConverter.class, Polygon2D.class);
        converterFactory.register(DateConverter.class, Date.class);
    }
    DatabaseQuery(DatabaseSource source, ParameterProvider parameterProvider) {
        super(source, parameterProvider);
    }

    @Override
    protected List<EOData> executeImpl() throws QueryException {
        List<EOData> results = new ArrayList<>();
        Connection sqlConnection = ((DatabaseSource) this.source).getConnection();
        if (sqlConnection != null) {
            try {
                String query = "SELECT * FROM " + PRODUCTS_TABLE + " P JOIN " + PRODUCT_PARAMS_TABLE + " PP " +
                        "ON P.ID = PP.ProductID WHERE ";
                int idx = 1;
                List<ParameterIndex> values = new ArrayList<>();
                for (Map.Entry<String, QueryParameter> entry : this.parameters.entrySet()) {
                    QueryParameter parameter = entry.getValue();
                    if (!parameter.isOptional() && !parameter.isInterval() && parameter.getValue() == null) {
                        throw new QueryException(String.format("Parameter [%s] is required but no value is supplied", parameter.getName()));
                    }
                    if (parameter.isOptional() &
                            ((!parameter.isInterval() & parameter.getValue() == null) |
                                    (parameter.isInterval() & parameter.getMinValue() == null & parameter.getMaxValue() == null))) {
                        continue;
                    }
                    if (idx > 1) {
                        query += " AND ";
                    }
                    query += "PP.Name='" + parameter.getName() + "'";
                    if (parameter.getType().isArray()) {
                        query += " AND PP.Value IN (";
                        Object value = parameter.getValue();
                        int length = Array.getLength(value);
                        Object[] arrayValue = new Object[length];
                        for (int i = 0; i < length; i++) {
                            query += "?";
                            if (i < length - 1) {
                                query += ",";
                            }
                            arrayValue[i] = Array.get(value, i);
                        }
                        query += ")";
                        values.add(new ParameterIndex(idx, idx + length - 1, arrayValue));
                        idx += length;
                    } else {
                        if (parameter.isInterval()) {
                            query += " AND PP.Value BETWEEN ? AND ?";
                            values.add(new ParameterIndex(idx, idx + 1,
                                                          parameter.getMinValue(),
                                                          parameter.getMaxValue()));
                            idx += 2;
                        } else {
                            if (!Polygon2D.class.equals(parameter.getType())) {
                                query += " AND PP.Value=?";
                                values.add(new ParameterIndex(idx, idx, parameter.getValue()));
                            } else {
                                query += " AND st_intersects(P." + parameter.getName() + ", st_geomfromwkt(?))";
                            }
                            idx += 1;
                        }
                    }
                }
                final PreparedStatement statement = sqlConnection.prepareStatement(query);
                for (ParameterIndex paramIndex : values) {
                    for (int i = paramIndex.fromIndex; i <= paramIndex.toIndex; i++) {
                        Object value = paramIndex.values[i];
                        Class clazz = value.getClass();
                        if (Byte.class.equals(clazz)) {
                            statement.setObject(i, value, Types.TINYINT);
                        } else if (Short.class.equals(clazz)) {
                            statement.setObject(i, value, Types.SMALLINT);
                        } else if (Integer.class.equals(clazz)) {
                            statement.setObject(i, value, Types.INTEGER);
                        } else if (Long.class.equals(clazz)) {
                            statement.setObject(i, value, Types.BIGINT);
                        } else if (Float.class.equals(clazz)) {
                            statement.setObject(i, value, Types.FLOAT);
                        } else if (Double.class.equals(clazz)) {
                            statement.setObject(i, value, Types.DOUBLE);
                        } else if (String.class.equals(clazz)) {
                            statement.setString(i, (String) value);
                        } else if (Polygon2D.class.equals(clazz)) {
                            statement.setString(i, ((Polygon2D) value).toWKT());
                        } else if (Date.class.equals(clazz)) {
                            statement.setObject(i, value, Types.DATE);
                        }
                    }
                }
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    EOProduct product = new EOProduct();
                    product.setId(resultSet.getString(1));
                    product.setName(resultSet.getString(2));
                    product.setAcquisitionDate(resultSet.getDate(3));
                    product.setSensorType(Enum.valueOf(SensorType.class, resultSet.getString(4)));
                    product.setPixelType(Enum.valueOf(PixelType.class, resultSet.getString(5)));
                    try {
                        product.setGeometry(new GeometryAdapter().marshal(resultSet.getString(6)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        product.setLocation(resultSet.getString(7));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    try {
                        product.setCrs(CRS.decode(resultSet.getString(8)));
                    } catch (FactoryException e) {
                        e.printStackTrace();
                    }
                    results.add(product);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return results;
    }

    @Override
    public String defaultName() {
        return "DatabaseQuery";
    }

    @Override
    public Identifiable copy() {
        return null;
    }

    private class ParameterIndex {
        Object[] values;
        int fromIndex;
        int toIndex;

        ParameterIndex(int fromIndex, int toIndex, Object... values) {
            this.values = values;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }
    }
}
