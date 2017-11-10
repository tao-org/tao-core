package ro.cs.tao.component.constraints;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public final class ConstraintFactory {

    private static final Map<String, Constraint> cache = new HashMap<>();

    public static <T> Constraint<T> create(String name) {
        try {
            if (!cache.containsKey(name)) {
                cache.put(name, (Constraint<T>) Class.forName(name).newInstance());
            }
            return cache.get(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
