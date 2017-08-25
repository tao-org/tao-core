package ro.cs.tao.component.validation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public enum ValidatorRegistry {
    INSTANCE;

    private final Map<Class<? extends Validator>, Validator> validators = new HashMap<Class<? extends Validator>, Validator>() {{
        put(NotNullValidator.class, new NotNullValidator());
        put(NotEmptyValidator.class, new NotEmptyValidator());
        put(TypeValidator.class, new TypeValidator());
        put(ValueSetValidator.class, new ValueSetValidator());
    }};

    public Validator getValidator(Class<? extends Validator> validatorClass) {
        return validators.get(validatorClass);
    }

    public void register(Class<? extends  Validator> validatorClass) {
        if (validatorClass != null) {
            if (CompositeValidator.class.equals(validatorClass)) {
                throw new IllegalArgumentException("CompositeValidator type is not registerable.");
            }
            if (!validators.containsKey(validatorClass)) {
                try {
                    final Constructor<? extends Validator> constructor = validatorClass.getConstructor();
                    validators.put(validatorClass, constructor.newInstance());
                } catch (NoSuchMethodException | IllegalAccessException
                        | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void unregister(Class<? extends  Validator> validatorClass) {
        if (validatorClass != null) {
            validators.remove(validatorClass);
        }
    }
}
