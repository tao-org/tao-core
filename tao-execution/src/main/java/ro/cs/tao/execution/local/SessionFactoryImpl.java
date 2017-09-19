package ro.cs.tao.execution.local;

import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

/**
 * @author Cosmin Cara
 */
public class SessionFactoryImpl extends SessionFactory {
    private Session thisSession;

    public SessionFactoryImpl() { }

    @Override
    public Session getSession() {
        synchronized (this) {
            if (thisSession == null) {
                thisSession = new SessionImpl();
            }
        }

        return thisSession;
    }
}
