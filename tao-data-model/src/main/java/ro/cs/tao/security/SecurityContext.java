package ro.cs.tao.security;

import java.security.Principal;

/**
 * @author Cosmin Cara
 */
public interface SecurityContext {
    Principal getPrincipal();
}
