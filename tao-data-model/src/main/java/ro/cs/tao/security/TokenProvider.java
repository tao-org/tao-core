package ro.cs.tao.security;

import ro.cs.tao.configuration.ConfigurationManager;

import java.util.UUID;

public interface TokenProvider {
    static int defaultExpiration() { return Integer.parseInt(ConfigurationManager.getInstance().getValue("local.token.expiration", "1800") ); }
    default Token newToken(String user, String password) { return new Token(UUID.randomUUID().toString(), UUID.randomUUID().toString(), defaultExpiration()); }
    default Token newToken(String refreshToken) { return new Token(UUID.randomUUID().toString(), UUID.randomUUID().toString(), defaultExpiration()); }
    default boolean validate(String token) { return true; }
}
