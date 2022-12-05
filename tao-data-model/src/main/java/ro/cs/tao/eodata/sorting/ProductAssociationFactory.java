package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ProductAssociationFactory {

    private static final Map<String, Class<? extends Association<EOProduct>>> associationMap;

    static {
        associationMap = new HashMap<>();
        Association<EOProduct> association = new ProductBySameDateAssociation();
        associationMap.put(association.friendlyName(), ProductBySameDateAssociation.class);
        association = new ProductByDateOffsetAssociation(0);
        associationMap.put(association.friendlyName(), ProductByDateOffsetAssociation.class);
    }

    public static Association<EOProduct> getAssociation(String name) {
        Class<? extends Association<EOProduct>> association = associationMap.get(name);
        if (association == null) {
            throw new IllegalArgumentException("No such Association<EOProduct>: " + name);
        }
        try {
            return association.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Cannot create association %s (are you missing an argument?)",
                                                             name));
        }
    }

    public static Association<EOProduct> getAssociation(String name, Object arg) {
        if (arg == null) {
            throw new IllegalArgumentException(String.format("Cannot create association %s with NULL argument", name));
        }
        Class<? extends Association<EOProduct>> association = associationMap.get(name);
        if (association == null) {
            throw new IllegalArgumentException("No such Association<EOProduct>: " + name);
        }
        try {
            Constructor<? extends Association<EOProduct>> constructor = association.getDeclaredConstructor(arg.getClass());
            return constructor.newInstance(arg);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalArgumentException(String.format("Cannot create association %s. Check the second argument",
                                                             name));
        }
    }
}
