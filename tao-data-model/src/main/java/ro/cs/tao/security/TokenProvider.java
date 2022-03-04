package ro.cs.tao.security;

import java.util.UUID;

public interface TokenProvider {
    static int defaultExpiration() { return 60; }
    default Token newToken(String user, String password) { return new Token(UUID.randomUUID().toString(), UUID.randomUUID().toString(), defaultExpiration()); }
    default Token newToken(String refreshToken) { return new Token(UUID.randomUUID().toString(), UUID.randomUUID().toString(), defaultExpiration()); }
    default boolean validate(String token) { return true; }
}
