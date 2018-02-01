package ro.cs.tao.component.converters;

import ro.cs.tao.component.ParameterDescriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class ConverterFactory {

    private final Map<Class, Class<? extends ParameterConverter>> converters;

    public static ConverterFactory getInstance() { return new ConverterFactory(); }

    private ConverterFactory() {
        converters = new HashMap<>();
        converters.put(Boolean.class, BooleanConverter.class);
        converters.put(Byte.class, ByteConverter.class);
        converters.put(Date.class, DateConverter.class);
        converters.put(Double.class, DoubleConverter.class);
        converters.put(Float.class, FloatConverter.class);
        converters.put(Integer.class, IntegerConverter.class);
        converters.put(Long.class, LongConverter.class);
        converters.put(Short.class, ShortConverter.class);
        converters.put(String.class, StringConverter.class);
    }

    public void register(Class<? extends ParameterConverter> converter, Class forClass) {
        converters.put(forClass, converter);
    }

    public void unregister(Class forClass) {
        converters.remove(forClass);
    }

    public ParameterConverter create(ParameterDescriptor parameter) {
        Class parameterType = parameter.getDataType();
        Class<? extends ParameterConverter> converterClass = converters.get(parameterType);
        if (converterClass == null) {
            converterClass = DefaultConverter.class;
        }
        Constructor<? extends ParameterConverter> ctor;
        ParameterConverter instance = null;
        try {
            ctor = converterClass.getConstructor(ParameterDescriptor.class);
            instance = ctor.newInstance(parameter);
        } catch (NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return instance;
    }
}