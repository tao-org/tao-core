package ro.cs.tao.security;

public interface TokenKeeper {
    default void evictExpired() { }

    default void put(Token token, String user) { }

    default Token getToken(String user) { return null; }

    default Token getFromRefreshToken(String refreshToken) { return null; }

    default Token getFullToken(String token) { return null; }

    default void remove(String user) { }
}
