package ro.cs.tao.execution.local;

import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

/**
 * @author Cosmin Cara
 */
public class DefaultSessionFactory extends SessionFactory {
    private DefaultSession thisSession;

    public DefaultSessionFactory() { }

    @Override
    public Session getSession() {
        synchronized (this) {
            if (thisSession == null) {
                thisSession = new DefaultSession();
                /*final List<NodeDescription> list = TopologyManager.getInstance().list();
                    if (list != null) {
                    int size = list.size();
                    NodeDescription[] nodes = new NodeDescription[size];
                    for (int i = 0; i < size; i++) {
                        this.nodes[i] = list.get(i);
                    }
                */
                //thisSession.setNodes(new NodeDescription[0]);
            }
        }

        return thisSession;
    }
}
