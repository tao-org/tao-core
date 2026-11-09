package ro.cs.tao.security;

/**
 * The TokenKeeper interface defines a contract for managing authentication tokens.
 * It provides default methods for operations such as adding, retrieving, and
 * removing tokens, as well as handling expired tokens.
 */
public interface TokenKeeper {
    /**
     * Removes all expired tokens from the underlying storage or system context.
     *
     * The `evictExpired` method ensures that tokens which have reached their
     * expiration time are no longer retained. This can prevent unnecessary
     * storage usage or security risks related to expired token reuse.
     *
     * Implementations of this method typically rely on the expiration metadata
     * associated with each token to determine its validity.
     */
    default void evictExpired() { }

    /**
     * Associates a token with a specified user for storage or further processing.
     * The method allows implementing classes to handle the mapping between
     * users and their corresponding authentication tokens.
     *
     * @param token the authentication token to be stored or processed.
     * @param user the identifier of the user to whom the token is associated.
     */
    default void put(Token token, String user) { }

    /**
     * Retrieves the authentication token associated with the specified user.
     *
     * @param user the identifier of the user whose token is to be retrieved.
     * @return the authentication token associated with the given user, or null if no token is found.
     */
    default Token getToken(String user) { return null; }

    /**
     * Retrieves a {@link Token} object associated with the provided refresh token.
     * This method can be used to exchange a refresh token for a new or existing authentication token.
     *
     * @param refreshToken the refresh token used to obtain the associated authentication token
     * @return the {@link Token} associated with the provided refresh token, or null if no token is found
     */
    default Token getFromRefreshToken(String refreshToken) { return null; }

    /**
     * Retrieves the complete {@link Token} object associated with the provided authentication token string.
     * The method allows implementations to resolve a token string into a fully detailed {@link Token} object,
     * including related attributes such as refresh token, expiration time, and ID token if available.
     *
     * @param token the authentication token string used to identify or retrieve the associated {@link Token} object
     * @return the {@link Token} object corresponding to the provided token string, or null if no such token is found
     */
    default Token getFullToken(String token) { return null; }

    /**
     * Removes the authentication token associated with the specified user.
     * This method allows the implementation to invalidate or delete
     * tokens for the given user from the underlying storage or system.
     *
     * @param user the identifier of the user whose token is to be removed
     */
    default void remove(String user) { }
}
