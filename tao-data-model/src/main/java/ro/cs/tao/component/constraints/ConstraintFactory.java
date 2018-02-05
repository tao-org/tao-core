package ro.cs.tao.component.constraints;

import org.reflections.Reflections;
import ro.cs.tao.eodata.EOData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Cosmin Cara
 */
public final class ConstraintFactory {

    private static final Map<String, IOConstraint> cache;
    private static final Map<String, String> aliases;

    static {
        cache = new HashMap<>();
        aliases = new HashMap<>();
        initAliases();
    }

    public static <T extends EOData> IOConstraint<T> create(String name) {
        String className = aliases.getOrDefault(name, name);
        try {
            if (!cache.containsKey(className)) {
                cache.put(className, (IOConstraint<T>) Class.forName(className).newInstance());
            }
            return cache.get(className);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getAvailableConstraints() {
        return new ArrayList<>(aliases.keySet());
    }

    private static void initAliases() {
        Reflections reflections = new Reflections("ro.cs.tao.component.constraints");
        final Set<Class<?>> annotatedTypes = reflections.getTypesAnnotatedWith(Constraint.class);
        annotatedTypes.forEach(type -> {
            String alias = type.getAnnotation(Constraint.class).name();
            aliases.put(alias, type.getName());
        });
    }

}
