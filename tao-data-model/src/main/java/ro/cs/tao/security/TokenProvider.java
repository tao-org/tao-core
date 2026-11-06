package ro.cs.tao.security;

import ro.cs.tao.configuration.ConfigurationManager;

import java.util.UUID;

/**
 * The TokenProvider interface defines methods for managing and validating authentication tokens.
 * It includes methods for creating new tokens, validating tokens, and handling user logout,
 * along with a default expiration configuration for tokens.
 *
 * @param <A> Represents the type associated with the implementation of the TokenProvider interface.
 */
public interface TokenProvider<A> {
    /**
     * Retrieves the default expiration time for tokens in seconds.
     * This value is obtained from the configuration settings using the key
     * "local.token.expiration". If the key is not found, a default value of
     * 1800 seconds is used.
     *
     * @return the default token expiration time in seconds.
     */
    static int defaultExpiration() { return Integer.parseInt(ConfigurationManager.getInstance().getValue("local.token.expiration", "1800") ); }
    /**
     * Creates a new authentication token for the provided user credentials.
     * The token includes a unique authentication token string and a refresh token.
     * The expiration time of the token is determined by the default expiration configuration.
     *
     * @param user the username or identifier of the user for whom the token is created
     * @param password the password associated with the user's account
     * @return a new {@link Token} instance containing the authentication token, refresh token, and expiration details
     */
    default Token newToken(String user, String password) { return new Token(UUID.randomUUID().toString(), UUID.randomUUID().toString(), defaultExpiration()); }
    /**
     * Creates a new authentication token for the provided user credentials and TOTP (Time-based One-Time Password).
     * The token includes a unique authentication token string and a refresh token.
     * The expiration time of the token is determined by the default expiration configuration.
     *
     * @param user the username or identifier of the user for whom the token is created
     * @param password the password associated with the user's account
     * @param totp the Time-based One-Time Password for multi-factor authentication
     * @return a new {@link Token} instance containing the authentication token, refresh token, and expiration details
     */
    default Token newToken(String user, String password, String totp) { return new Token(UUID.randomUUID().toString(), UUID.randomUUID().toString(), defaultExpiration()); }
    /**
     * Creates a new authentication token using the provided refresh token.
     * The refresh token is used to generate a new authentication token and associated details.
     * The expiration time of the token is determined by the default expiration configuration.
     *
     * @param refreshToken the refresh token used to obtain a new authentication token
     * @return a new {@link Token} instance containing the authentication token, refresh token, and expiration details
     */
    default Token newToken(String refreshToken) { return new Token(UUID.randomUUID().toString(), UUID.randomUUID().toString(), defaultExpiration()); }
    /**
     * Creates a new authentication token using an authorization code.
     * The token will consist of a unique authentication token string,
     * a refresh token, and an expiration time determined by the default configuration.
     *
     * @param code the authorization code used to create the token
     * @return a new {@link Token} instance containing the authentication token,
     *         refresh token, and expiration details
     */
    default Token newTokenFromCode(String code) { return new Token(UUID.randomUUID().toString(), UUID.randomUUID().toString(), defaultExpiration()); }
    /**
     * Validates the specified token to determine its authenticity or correctness.
     *
     * @param token the token to be validated
     * @return true if the token is valid, false otherwise
     */
    default boolean validate(String token) { return true; }
    /**
     * Logs out the specified user, invalidating their current authenticated session or token.
     *
     * @param user the username or identifier of the user to be logged out
     * @return true if the logout operation was successful, false otherwise
     */
    default boolean logout(String user) { return true; }
}
