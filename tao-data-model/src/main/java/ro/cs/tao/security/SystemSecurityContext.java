package ro.cs.tao.security;

import java.security.Principal;

/**
 * @author Cosmin Cara
 */
public class SystemSecurityContext implements SecurityContext {
    private static final SecurityContext instance = new SystemSecurityContext();

    public static SecurityContext instance() { return instance; }

    private SystemSecurityContext() { }

    @Override
    public Principal getPrincipal() {
        return SystemPrincipal.instance();
    }
}
