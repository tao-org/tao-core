package ro.cs.tao.workspaces;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.topology.TopologyException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RepositoryFactory {
    private static final RepositoryFactory instance;
    private final Map<RepositoryType, Repository> templates;

    static {
        instance = new RepositoryFactory();
    }

    private RepositoryFactory() {
        this.templates = new HashMap<>();
        final String values = ConfigurationManager.getInstance().getValue("workspace.enabled");
        if (values != null) {
            final String[] workspaces = values.toUpperCase().split(",");
            for (String w : workspaces) {
                RepositoryType type = EnumUtils.getEnumConstantByName(RepositoryType.class, w);
                Repository workspace = new Repository();
                workspace.setType(type);
                workspace.setName(type.name());
                workspace.setDescription(type.friendlyName());
                workspace.setUrlPrefix(type.prefix());
                final Map<String, Boolean> parameters = type.getParameters();
                for (Map.Entry<String, Boolean> entry : parameters.entrySet()) {
                    workspace.addParameter(entry.getKey(), "");
                }
                this.templates.put(type, workspace);
            }
        }
    }

    public static Repository createDefault(RepositoryType type, String user) {
        final Repository workspace = instance.templates.get(type);
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace type " + type.name() + " is disabled");
        }
        workspace.setId(UUID.randomUUID().toString());
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

    public static Repository createFromTemplate(RepositoryTemplate template, String user) {
        final Repository workspace = template.toRepository();
        workspace.setUserName(user);
        final ConfigurationProvider cfgProvider = ConfigurationManager.getInstance();
        RepositoryType type = workspace.getType();
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
        workspace.setSystem(false);
        workspace.setReadOnly(true);
        return workspace;
    }
}
