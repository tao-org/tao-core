package ro.cs.tao.topology;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorkspaceFactory {
    private static final WorkspaceFactory instance;
    private final Map<WorkspaceType, Workspace> templates;

    static {
        instance = new WorkspaceFactory();
    }

    private WorkspaceFactory() {
        this.templates = new HashMap<>();
        final String values = ConfigurationManager.getInstance().getValue("workspace.enabled");
        if (values != null) {
            final String[] workspaces = values.toUpperCase().split(",");
            for (String w : workspaces) {
                WorkspaceType type = EnumUtils.getEnumConstantByName(WorkspaceType.class, w);
                Workspace workspace = new Workspace();
                workspace.setType(type);
                workspace.setName(type.name());
                workspace.setDescription(type.friendlyName());
                workspace.setUrlPrefix(type.prefix());
                final Set<String> parameters = type.getParameters();
                for (String param : parameters) {
                    workspace.addParameter(param, "");
                }
                this.templates.put(type, workspace);
            }
        }
    }

    public static Workspace createDefault(WorkspaceType type, String user) {
        final Workspace workspace = instance.templates.get(type);
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace type " + type.name() + " is disabled");
        }
        workspace.setUserName(user);
        final ConfigurationProvider cfgProvider = ConfigurationManager.getInstance();
        switch (type) {
            case LOCAL:
                workspace.getParameters().replace(type.rootKey(),
                                                  cfgProvider.getValue("workspace.location") + "/" + user);
                break;
            case AWS:
            case SWIFT:
                final HashMap<String, String> parameters = workspace.getParameters();
                String value;
                for (String key : parameters.keySet()) {
                    if (type.rootKey().equals(key)) {
                        value = cfgProvider.getValue(type.rootKey());
                        if (value != null) {
                            value += "/" + user;
                        }
                    } else {
                        value = cfgProvider.getValue(key);
                    }
                    if (value == null) {
                        throw new TopologyException("Values for default workspace of type" + type.name() + " not configured");
                    }
                    parameters.replace(key, value);
                }
                break;
            default:
                break;
        }
        workspace.setSystem(true);
        return workspace;
    }
}
