package ro.cs.tao.security;

import java.util.UUID;

public interface TokenProvider {
    default String newToken(String user, String password) { return UUID.randomUUID().toString(); }
    default boolean validateToken(String token) { return true; }
}
