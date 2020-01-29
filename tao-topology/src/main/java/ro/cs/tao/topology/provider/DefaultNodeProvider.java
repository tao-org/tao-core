package ro.cs.tao.topology.provider;

import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeFlavor;
import ro.cs.tao.topology.NodeProvider;
import ro.cs.tao.topology.TopologyException;
import ro.cs.tao.utils.async.LazyInitialize;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Default implementation for topology nodes provider, using the TAO database as "provider".
 *
 * @author Cosmin Cara
 */
public class DefaultNodeProvider implements NodeProvider {
    protected final Logger logger;
    private Supplier<PersistenceManager> persistenceManager;

    public DefaultNodeProvider() {
        this.logger = Logger.getLogger(DefaultNodeProvider.class.getName());
        this.persistenceManager = LazyInitialize.using(() -> SpringContextBridge.services().getService(PersistenceManager.class));
    }

    @Override
    public void authenticate() {
        // No-op
    }

    @Override
    public List<NodeFlavor> listFlavors() throws TopologyException {
        return null;
    }

    @Override
    public List<NodeDescription> listNodes() throws TopologyException {
        return getPersistenceManager().nodes().list(true);
    }

    @Override
    public NodeDescription getNode(String nodeName) throws TopologyException {
        return getPersistenceManager().nodes().get(nodeName);
    }

    @Override
    public NodeDescription create(NodeDescription node) throws TopologyException {
        if (node == null) {
            throw new TopologyException("null");
        }
        if (getPersistenceManager().nodes().exists(node.getId())) {
            try {
                node = getPersistenceManager().nodes().update(node);
            } catch (Exception e) {
                logger.severe("Cannot update node description to database. Rolling back installation on node " + node.getId() + "...");
                throw new TopologyException(e);
            }
        } else {
            try {
                node = getPersistenceManager().nodes().save(node);
            } catch (PersistenceException e) {
                logger.severe("Cannot save node description to database. Rolling back installation on node " + node.getId() + "...");
                throw new TopologyException(e);
            }
        }
        return node;
    }

    @Override
    public NodeDescription create(NodeFlavor flavor, String name, String description, String user, String pwd) throws TopologyException {
        NodeDescription info = new NodeDescription();
        info.setId(name);
        info.setDescription(description);
        info.setUserName(user);
        info.setUserPass(pwd);
        info.setFlavor(flavor);
        return create(info);
    }

    @Override
    public void remove(String nodeName) throws TopologyException {
        NodeDescription node = getNode(nodeName);
        if (node == null) {
            throw new TopologyException(String.format("Node [%s] does not exist", nodeName));
        }
        try {
            getPersistenceManager().nodes().delete(nodeName);
        } catch (PersistenceException e) {
            logger.severe("Cannot remove node description from database. Host name is :" + nodeName);
            throw new TopologyException(e);
        }
    }

    @Override
    public void suspend(String nodeName) throws TopologyException {
        NodeDescription node = getNode(nodeName);
        if (node != null) {
            node.setActive(false);
            update(node);
        }
    }

    @Override
    public void resume(String nodeName) throws TopologyException {
        NodeDescription node = getNode(nodeName);
        if (node != null) {
            node.setActive(true);
            update(node);
        }
    }

    @Override
    public NodeDescription update(NodeDescription node) throws TopologyException {
        try {
            return getPersistenceManager().nodes().update(node);
        } catch (Exception e) {
            logger.severe(String.format("Cannot update node description in database [host:%s; reason:%s]",
                                        node != null ? node.getId() : "null", e.getMessage()));
            throw new TopologyException(e);
        }
    }

    private PersistenceManager getPersistenceManager() {
        return this.persistenceManager.get();
    }
}
