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
 * @author Cosmin Cara
 */
public class JsonResponseParser<T> implements ResponseParser<T> {
    private static final Logger logger = Logger.getLogger(JsonResponseParser.class.getName());
    private final JSonResponseHandler<T> handler;

    public JsonResponseParser(JSonResponseHandler<T> handler) {
        this.handler = handler;
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
//                Attribute[] attrs = r.getAttributes();
//                List<Attribute> attributes = Arrays.stream(attrs).collect(Collectors.toList());
                if (res instanceof EOData) {
                    EOData r = (EOData) res;
                    List<Attribute> attributes = r.getAttributes();
                    int idx = 0;
                    while (idx < attributes.size()) {
                        Attribute attribute = attributes.get(idx);
                        if (!filter.accept(attribute.getName(), attribute.getValue())) {
                            attributes.remove(idx);
                        } else {
                            idx++;
                        }
                    }
//                if (attrs.length != attributes.size()) {
//                    r.setAttributes(attributes.toArray(new Attribute[attributes.size()]));
//                }

                    if (r.getAttributes().size() != attributes.size()) {
                        r.setAttributes(attributes);
                    }
                }
            });
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
        return result;
    }
}
