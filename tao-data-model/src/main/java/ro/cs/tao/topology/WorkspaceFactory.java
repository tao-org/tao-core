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

    public static Workspace create(WorkspaceType type, String user) {
        final Workspace workspace = instance.templates.get(type);
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace type " + type.name() + " is disabled");
        }
        workspace.setUserName(user);
        final ConfigurationProvider cfgProvider = ConfigurationManager.getInstance();
        switch (type) {
            case LOCAL:
                workspace.getParameters().replace("root",
                                                  cfgProvider.getValue("workspace.location") + "/" + user);
                break;
            case AWS:
                workspace.getParameters().replace("aws.bucket",
                                                  cfgProvider.getValue("aws.bucket") + "/user");
                workspace.getParameters().replace("aws.secret.key", cfgProvider.getValue("aws.secret.key"));
                workspace.getParameters().replace("aws.access.key", cfgProvider.getValue("aws.access.key"));
                break;
            case SWIFT:
            default:
                break;
        }
        return workspace;
    }
}
