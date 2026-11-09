package ro.cs.tao.security;

/**
 * Represents a token used for authentication and authorization purposes.
 * A token typically contains information about its expiration time and may
 * include additional components such as a refresh token and an ID token.
 *
 * Instances of this class are immutable, ensuring thread safety.
 */
public class Token {
    private final String token;
    private final String refreshToken;
    private final String idToken;
    private final int expiresInSeconds;
    private final long expiration;

    public Token(String token, String refreshToken, int expiresInSeconds) {
        this(token, refreshToken, null, expiresInSeconds);
    }

    public Token(String token, String refreshToken, String idToken, int expiresInSeconds) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.idToken = idToken;
        this.expiresInSeconds = expiresInSeconds;
        this.expiration = System.currentTimeMillis() + this.expiresInSeconds * 1000L;
    }

    /**
     * Retrieves the authentication token.
     *
     * @return the authentication token string.
     */
    public String getToken() {
        return token;
    }

    /**
     * Retrieves the refresh token associated with this instance.
     *
     * @return the refresh token string used to obtain a new authentication token.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Retrieves the ID token associated with this instance.
     * The ID token typically contains user identity information
     * encoded in a secure format such as JWT (JSON Web Token).
     *
     * @return the ID token string, or null if no ID token is available.
     */
    public String getIdToken() {
        return idToken;
    }

    /**
     * Retrieves the number of seconds until the token expires.
     *
     * @return the expiration time of the token in seconds.
     */
    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    /**
     * Determines whether the token has expired based on the current time.
     *
     * @return true if the token's expiration time has passed, false otherwise.
     */
    public boolean isExpired() {
        return this.expiration < System.currentTimeMillis();
    }

}
