package ro.cs.tao.security;

public class Token {
    private final String token;
    private final String refreshToken;
    private final int expiresInSeconds;
    private final long expiration;

    public Token(String token, String refreshToken, int expiresInSeconds) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresInSeconds = expiresInSeconds;
        this.expiration = System.currentTimeMillis() + this.expiresInSeconds * 1000L;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public boolean isExpired() {
        return this.expiration < System.currentTimeMillis();
    }

}
