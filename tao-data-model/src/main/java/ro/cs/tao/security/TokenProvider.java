package ro.cs.tao.security;

import java.util.UUID;

public interface TokenProvider {
    static int defaultExpiration() { return 3600; }
    default Token newToken(String user, String password) { return new Token(UUID.randomUUID().toString(), null, defaultExpiration()); }
    default Token newToken(String refreshToken) { return new Token(UUID.randomUUID().toString(), null, defaultExpiration()); }
    default boolean validate(String token) { return true; }
}
