package ro.cs.tao.topology.provider;

import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeFlavor;
import ro.cs.tao.topology.TopologyException;
import ro.cs.tao.utils.async.LazyInitialize;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        return getPersistenceManager().getNodes().stream()
                .filter(NodeDescription::getActive)     // we take only active node but not the deleted ones
                .collect(Collectors.toList());
    }

    @Override
    public NodeDescription getNode(String nodeName) throws TopologyException {
        try {
            return getPersistenceManager().getNodeByHostName(nodeName);
        } catch (PersistenceException e) {
            logger.severe("Cannot get node description from database for node " + nodeName);
            throw new TopologyException(e);
        }
    }

    @Override
    public NodeDescription addNode(NodeDescription node) throws TopologyException {
        if (node == null) {
            throw new TopologyException("null");
        }
        if (getPersistenceManager().checkIfExistsNodeByHostName(node.getId())) {
            try {
                node = getPersistenceManager().updateExecutionNode(node);
            } catch (PersistenceException e) {
                logger.severe("Cannot update node description to database. Rolling back installation on node " + node.getId() + "...");
                throw new TopologyException(e);
            }
        } else {
            try {
                node = getPersistenceManager().saveExecutionNode(node);
            } catch (PersistenceException e) {
                logger.severe("Cannot save node description to database. Rolling back installation on node " + node.getId() + "...");
                throw new TopologyException(e);
            }
        }
        return node;
    }

    @Override
    public NodeDescription addNode(NodeFlavor flavor, String name, String description, String user, String pwd) throws TopologyException {
        NodeDescription info = new NodeDescription();
        info.setId(name);
        info.setDescription(description);
        info.setUserName(user);
        info.setUserPass(pwd);
        info.setFlavor(flavor);
        return addNode(info);
    }

    @Override
    public void removeNode(String nodeName) throws TopologyException {
        NodeDescription node = getNode(nodeName);
        if (node == null) {
            throw new TopologyException(String.format("Node [%s] does not exist", nodeName));
        }
        try {
            getPersistenceManager().deleteExecutionNode(nodeName);
        } catch (PersistenceException e) {
            logger.severe("Cannot remove node description from database. Host name is :" + nodeName);
            throw new TopologyException(e);
        }
    }

    @Override
    public void suspendNode(String nodeName) throws TopologyException {
        NodeDescription node = getNode(nodeName);
        if (node != null) {
            node.setActive(false);
            updateNode(node);
        }
    }

    @Override
    public void resumeNode(String nodeName) throws TopologyException {
        NodeDescription node = getNode(nodeName);
        if (node != null) {
            node.setActive(true);
            updateNode(node);
        }
    }

    @Override
    public NodeDescription updateNode(NodeDescription node) throws TopologyException {
        try {
            return getPersistenceManager().updateExecutionNode(node);
        } catch (PersistenceException e) {
            logger.severe(String.format("Cannot update node description in database [host:%s; reason:%s]",
                                        node != null ? node.getId() : "null", e.getMessage()));
            throw new TopologyException(e);
        }
    }

    private PersistenceManager getPersistenceManager() {
        return this.persistenceManager.get();
    }
}
