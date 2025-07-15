package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating instances of EOProduct associations.
 * This class provides a mechanism to dynamically create association objects based on their
 * corresponding friendly names and optional arguments. It uses an internal mapping of association
 * names to their implementing classes to support the creation of these association objects.
 *
 * Associations are used to group or pair EOProduct objects based on certain criteria defined by
 * the specific association implementation.
 *
 * Key functionality:
 * - Registers and maps association types with corresponding friendly names.
 * - Supports creation of association instances without arguments.
 * - Supports creation of association instances with a single constructor argument.
 * - Provides error handling for missing associations, invalid arguments, or instantiation issues.
 */
public class ProductAssociationFactory {

    private static final Map<String, Class<? extends Association<EOProduct>>> associationMap;

    static {
        associationMap = new HashMap<>();
        Association<EOProduct> association = new ProductBySameDateAssociation();
        associationMap.put(association.friendlyName(), ProductBySameDateAssociation.class);
        association = new ProductByDateOffsetAssociation(0);
        associationMap.put(association.friendlyName(), ProductByDateOffsetAssociation.class);
        association = new PreviousProductAssociation();
        associationMap.put(association.friendlyName(), PreviousProductAssociation.class);
    }

    /**
     * Retrieves an instance of an Association for EOProduct based on the provided association name.
     * If the specified association name is not registered, an IllegalArgumentException is thrown.
     * The method dynamically instantiates the Association object for the corresponding name.
     *
     * @param name the name of the requested Association. It must match an entry in the associationMap.
     * @return an instance of the requested Association<EOProduct>.
     * @throws IllegalArgumentException if the association name is not found or the instantiation fails.
     */
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

    /**
     * Retrieves an instance of an Association for EOProduct based on the provided association name and argument.
     * If the specified association name is not registered or the argument is null, an IllegalArgumentException is thrown.
     * The method dynamically instantiates the Association object for the corresponding name using the provided argument.
     *
     * @param name the name of the requested Association. It must match an entry in the associationMap.
     * @param arg the argument to be passed to the constructor of the Association. It must not be null.
     * @return an instance of the requested Association<EOProduct>.
     * @throws IllegalArgumentException if the association name is not found, the argument is null, or the instantiation fails.
     */
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
