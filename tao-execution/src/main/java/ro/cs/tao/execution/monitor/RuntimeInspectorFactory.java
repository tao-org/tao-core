package ro.cs.tao.execution.monitor;

import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.NodeRole;
import ro.cs.tao.utils.executors.AuthenticationType;

import java.util.Set;

public final class RuntimeInspectorFactory {
    private static final RuntimeInspectorFactory instance;
    private final Set<NodeRuntimeInspector> inspectors;

    static {
        instance = new RuntimeInspectorFactory();
    }

    public static RuntimeInspectorFactory getInstance() {
        return instance;
    }

    private RuntimeInspectorFactory() {
        this.inspectors = ServiceRegistryManager.getInstance().getServiceRegistry(NodeRuntimeInspector.class).getServices();
    }

    public NodeRuntimeInspector get(String host, NodeRole nodeRole, AuthenticationType authType, String user, String secret) throws Exception {
        final NodeRuntimeInspector inspector = this.inspectors.stream().filter(i -> i.isIntendedFor(nodeRole)).findFirst().orElse(null);
        if (inspector == null) {
            throw new RuntimeException("No runtime inspector found for role " + nodeRole.name());
        }
        inspector.initialize(host, user, secret, authType);
        return inspector;
    }


}
