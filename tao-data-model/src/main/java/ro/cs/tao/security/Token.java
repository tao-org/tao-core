package ro.cs.tao.security;

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

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public boolean isExpired() {
        return this.expiration < System.currentTimeMillis();
    }

}
