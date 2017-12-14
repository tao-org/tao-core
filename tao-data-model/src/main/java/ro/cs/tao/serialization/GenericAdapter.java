package ro.cs.tao.serialization;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

/**
 * @author Cosmin Cara
 */
public class GenericAdapter extends XmlAdapter<Object, String> {
    private Class clazz;

    public GenericAdapter(String className) {
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("No such class");
        }
    }

    @Override
    public String unmarshal(Object v) throws Exception {
        return !clazz.isAssignableFrom(Date.class) ? String.valueOf(v) : new DateAdapter().marshal((Date) v);
    }

    @Override
    public Object marshal(String v) throws Exception {
        if (clazz.isAssignableFrom(String.class)) {
            return v;
        } else  if (clazz.isAssignableFrom(Byte.class)) {
            return Byte.parseByte(v);
        } else if (clazz.isAssignableFrom(Short.class)) {
            return Short.parseShort(v);
        } else if (clazz.isAssignableFrom(Integer.class)) {
            return Integer.parseInt(v);
        } else if (clazz.isAssignableFrom(Long.class)) {
            return Long.parseLong(v);
        } else if (clazz.isAssignableFrom(Float.class)) {
            return Float.parseFloat(v);
        } else if (clazz.isAssignableFrom(Double.class)) {
            return Double.parseDouble(v);
        } else if (clazz.isAssignableFrom(Date.class)) {
            return new DateAdapter().unmarshal(v);
        } else {
            return null;
        }
    }
}
