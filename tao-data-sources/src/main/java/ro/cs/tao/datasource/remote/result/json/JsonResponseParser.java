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
package ro.cs.tao.datasource.remote.result.json;

import ro.cs.tao.datasource.remote.result.ParseException;
import ro.cs.tao.datasource.remote.result.ResponseParser;
import ro.cs.tao.datasource.remote.result.filters.CompositeFilter;
import ro.cs.tao.datasource.remote.result.filters.NameFilter;
import ro.cs.tao.datasource.remote.result.filters.NullFilter;
import ro.cs.tao.datasource.remote.result.filters.ValueFilter;
import ro.cs.tao.eodata.Attribute;
import ro.cs.tao.eodata.EOData;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Parser for JSON documents.
 *
 * @author Cosmin Cara
 */
public class JsonResponseParser<T> implements ResponseParser<T> {
    private static final Logger logger = Logger.getLogger(JsonResponseParser.class.getName());
    private final JSonResponseHandler<T> handler;
    private final String countPropertyName;

    public JsonResponseParser(JSonResponseHandler<T> handler) {
        this.handler = handler;
        this.countPropertyName = "";
    }

    public JsonResponseParser(JSonResponseHandler<T> handler, String countPropertyName) {
        this.handler = handler;
        this.countPropertyName = countPropertyName;
    }

    @Override
    public T parseValue(String content) throws ParseException {
        T result = null;
        try {
            CompositeFilter filter = new CompositeFilter();
            filter.addFilter(new NullFilter());
            filter.addFilter(new ValueFilter("null"));
            String[] excludedAttributes = this.getExcludedAttributes();
            if (excludedAttributes != null && excludedAttributes.length > 0) {
                filter.addFilter(new NameFilter(Arrays.stream(excludedAttributes)
                                                        .collect(Collectors.toSet())));
            }
            result = handler.readValue(content);
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
        return result;
    }

    @Override
    public List<T> parse(String content) throws ParseException {
        List<T> result = null;
        try {
            CompositeFilter filter = new CompositeFilter();
            filter.addFilter(new NullFilter());
            filter.addFilter(new ValueFilter("null"));
            String[] excludedAttributes = this.getExcludedAttributes();
            if (excludedAttributes != null && excludedAttributes.length > 0) {
                filter.addFilter(new NameFilter(Arrays.stream(excludedAttributes)
                                                                 .collect(Collectors.toSet())));
            }
            result = handler.readValues(content);
            result.forEach(res -> {
                if (res instanceof EOData) {
                    EOData r = (EOData) res;
                    List<Attribute> attributes = r.getAttributes();
                    int idx = 0;
                    if (attributes != null) {
                        while (idx < attributes.size()) {
                            Attribute attribute = attributes.get(idx);
                            if (!filter.accept(attribute.getName(), attribute.getValue())) {
                                attributes.remove(idx);
                            } else {
                                idx++;
                            }
                        }
                        if (r.getAttributes().size() != attributes.size()) {
                            r.setAttributes(attributes);
                        }
                    }
                }
            });
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
        return result;
    }

    @Override
    public long parseCount(String content) throws ParseException {
        if (this.countPropertyName != null && !this.countPropertyName.isEmpty()) {
            try {
                return this.handler.countValues(content);
            } catch (IOException e) {
                throw new ParseException(e);
            }
        }
        return 0;
    }
}
