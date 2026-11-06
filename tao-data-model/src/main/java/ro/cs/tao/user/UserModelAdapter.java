package ro.cs.tao.user;

/**
 * This interface provides a contract for converting between a platform-specific user representation
 * and a standard application User object, and vice versa. It allows implementations to adapt
 * entity models from different systems or libraries into a unified User model used in the application.
 *
 * @param <T> the type of the platform-specific user representation to be adapted
 */
public interface UserModelAdapter <T> {
    /**
     * Converts a platform-specific user representation to a standard {@code User} object.
     *
     * @param object the platform-specific user representation to be converted
     * @return the converted {@code User} object
     */
    User toTaoUser(T object);
    /**
     * Converts a standard {@code User} object into a platform-specific user representation.
     *
     * @param user the {@code User} object to be converted
     * @return the platform-specific user representation derived from the input {@code User} object
     */
    T fromTaoUser(User user);
}
