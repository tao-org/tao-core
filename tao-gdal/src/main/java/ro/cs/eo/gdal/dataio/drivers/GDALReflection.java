package ro.cs.eo.gdal.dataio.drivers;

import ro.cs.eo.gdal.dataio.GDALLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * GDAL Reflection class which uses Java reflection API to invoke methods from JNI GDAL
 */
class GDALReflection {
    private static final Map<String, Integer> cachedConstants = Collections.synchronizedMap(new HashMap<>());
    /**
     * Creates new instance for this class
     */
    private GDALReflection() {
        //nothing to init
    }

    /**
     * Fetches the GDAL constants from JNI GDAL class
     *
     * @param className    the target JNI GDAL class name
     * @param constantName the target GDAL constant name
     * @return the GDAL constant
     */
    static Integer fetchGDALLibraryConstant(String className, String constantName) {
        try {
            if (!cachedConstants.containsKey(constantName)) {
                Class<?> gdalconstConstantsClass = fetchGDALLibraryClass(className);
                cachedConstants.put(constantName, (Integer) gdalconstConstantsClass.getField(constantName).get(null));
            }
            return cachedConstants.get(constantName);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Calls the JNI GDAL class method using Java reflection API
     *
     * @param className      the target JNI GDAL class name
     * @param methodName     the target JNI GDAL class method name
     * @param returnType     the target GDAL class method return type
     * @param instance       the target JNI GDAL class instance
     * @param argumentsTypes the target GDAL class method arguments types
     * @param arguments      the target GDAL class method arguments values
     * @param <T>            the target GDAL data type parameter
     * @return the result returned by JNI GDAL class method
     */
    static <T> T callGDALLibraryMethod(String className, String methodName, Class<T> returnType, Object instance, Class[] argumentsTypes, Object[] arguments) {
        try {
            Class<?> gdalClass = fetchGDALLibraryClass(className);
            Method gdalClassMethod = gdalClass.getMethod(methodName, argumentsTypes);
            Object returnResult = gdalClassMethod.invoke(instance, arguments);
            if (returnResult != null && returnType != null) {
                return returnType.cast(returnResult);
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return null;
    }

    static Class<?> fetchGDALLibraryClass(String className) {
        try {
            return Class.forName(className, false, GDALLoader.getInstance().getGDALVersionLoader());
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    static Object fetchGDALLibraryClassInstance(String className, Class[] argumentsTypes, Object[] arguments) {
        try {
            Class<?> gdalClass = fetchGDALLibraryClass(className);
            Constructor gdalClassConstructor = gdalClass.getConstructor(argumentsTypes);
            return gdalClassConstructor.newInstance(arguments);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
