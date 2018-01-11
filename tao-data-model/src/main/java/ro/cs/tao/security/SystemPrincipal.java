package ro.cs.tao.security;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * @author Cosmin Cara
 */
public final class SystemPrincipal implements Principal {

    private static final SystemPrincipal instance = new SystemPrincipal();

    public static Principal instance() { return instance; }

    @Override
    public String getName() {
        return "System Account";
    }

    @Override
    public boolean implies(Subject subject) {
        return true;
    }
}
