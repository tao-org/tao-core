package ro.cs.tao.workspaces;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.topology.TopologyException;

import java.util.*;

/**
 * The RepositoryFactory class is responsible for creating and managing repository objects
 * of various repository types. It provides methods to create default repositories of specific
 * types, either with predefined parameters or customized configurations.
 *
 * This class uses a singleton pattern to ensure a single instance throughout the application,
 * facilitating central management of repository templates for different repository types.
 *
 * The factory supports repository types such as LOCAL, AWS, and SWIFT, and provides utility
 * methods to generate repository instances based on the types and associated configurations.
 */
public class RepositoryFactory {
    private static final RepositoryFactory instance;
    private final Map<RepositoryType, Repository> templates;

    static {
        instance = new RepositoryFactory();
    }

    private RepositoryFactory() {
        this.templates = new HashMap<>();
        final String values = ConfigurationManager.getInstance().getValue("workspaces.enabled");
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

    /**
     * Retrieves the default set of repository types available in the system.
     *
     * @return a set containing all default {@code RepositoryType} values configured in the {@code templates} field of the repository factory instance.
     */
    public static Set<RepositoryType> getDefaultRepositoryTypes() {
        return new HashSet<>(instance.templates.keySet());
    }

    /**
     * Creates a default repository based on the provided type and user-specific parameters.
     * This method initializes a new {@code Repository} instance using a pre-configured template
     * of the specified {@code RepositoryType}. It sets user-specific properties, such as the
     * workspace location or other parameters, based on the input details and configuration.
     *
     * @param type the type of the repository to be created; must not be null.
     * @param userId the unique identifier of the user for whom the repository is being created; must not be null.
     * @param dbParams a map of database parameters that may override or supplement the default configuration; may be null.
     * @return the newly created {@code Repository} instance configured with the specified type and user-specific data.
     * @throws IllegalArgumentException if the specified {@code RepositoryType} is not enabled or supported.
     * @throws TopologyException if required configuration values for the specified type are missing.
     */
    public static Repository createDefault(RepositoryType type, String userId, Map<String, String> dbParams) {
        final Repository workspace = instance.templates.get(type);
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace type " + type.name() + " is disabled");
        }
        workspace.setId(UUID.randomUUID().toString());
        workspace.setUserId(userId);
        final ConfigurationProvider cfgProvider = ConfigurationManager.getInstance();
        if (dbParams == null) {
            dbParams = new HashMap<>();
        }
        switch (type) {
            case LOCAL:
                workspace.getParameters().replace(type.rootKey(),
                                                  cfgProvider.getValue("workspace.location") + "/" + userId);
                break;
            case AWS:
                final HashMap<String, String> parameters = workspace.getParameters();
                String value;
                for (String key : parameters.keySet()) {
                    if (type.rootKey().equals(key)) {
                        value = dbParams.containsKey(key) ? dbParams.get(key) : cfgProvider.getValue(type.rootKey());
                        if (value != null) {
                            value += "/" + userId;
                        }
                    } else {
                        value = dbParams.containsKey(key) ? dbParams.get(key) : cfgProvider.getValue(key);
                    }
                    if (value == null) {
                        throw new TopologyException("Values for default workspace of type " + type.name() + " not configured");
                    }
                    parameters.replace(key, value);
                }
                break;
            case SWIFT:
                final HashMap<String, String> params = workspace.getParameters();
                params.replace(type.rootKey(), userId);
                params.entrySet().removeIf(e -> !e.getKey().equals(type.rootKey()));
                break;
            default:
                break;
        }
        workspace.setSystem(true);
        return workspace;
    }

    /**
     * Creates a default repository based on the provided type and user-specific parameters.
     * This method initializes a new {@code Repository} instance using a pre-configured template
     * of the specified {@code RepositoryType}. It sets user-specific properties, such as
     * the workspace location or other parameters, based on the input details and configuration.
     *
     * @param type the type of the repository to be created; must not be null.
     * @param userId the unique identifier of the user for whom the repository is being created; must not be null.
     * @param userName the name of the user for whom the repository is being created; must not be null.
     * @param dbParams a map of database parameters that may override or supplement the default configuration; may be null.
     * @return the newly created {@code Repository} instance configured with the specified type and user-specific data.
     * @throws IllegalArgumentException if the specified {@code RepositoryType} is not enabled or supported.
     * @throws TopologyException if required configuration values for the specified type are missing.
     */
    public static Repository createDefault(RepositoryType type, String userId, String userName, Map<String, String> dbParams) {
        final Repository workspace = instance.templates.get(type);
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace type " + type.name() + " is disabled");
        }
        workspace.setId(UUID.randomUUID().toString());
        workspace.setUserId(userId);
        final ConfigurationProvider cfgProvider = ConfigurationManager.getInstance();
        if (dbParams == null) {
            dbParams = new HashMap<>();
        }
        switch (type) {
            case LOCAL:
                workspace.getParameters().replace(type.rootKey(),
                                                  cfgProvider.getValue("workspace.location") + "/" + userId);
                break;
            case AWS:
                final HashMap<String, String> parameters = workspace.getParameters();
                final Map<String, Boolean> typeParameters = type.getParameters();
                String value;
                for (String key : parameters.keySet()) {
                    if (type.rootKey().equals(key)) {
                        value = dbParams.containsKey(key) ? dbParams.get(key) : cfgProvider.getValue(type.rootKey());
                        if (value != null) {
                            value += "/" + userName;
                        }
                    } else {
                        value = dbParams.containsKey(key) ? dbParams.get(key) : cfgProvider.getValue(key);
                    }
                    if (value == null && typeParameters.get(key)) {
                        throw new TopologyException("Value [" + key + "] for default workspace of type " + type.name() + " not configured");
                    }
                    parameters.replace(key, value);
                }
                break;
            case SWIFT:
                final HashMap<String, String> params = workspace.getParameters();
                params.replace(type.rootKey(), userId);
                params.entrySet().removeIf(e -> !e.getKey().equals(type.rootKey()));
                break;
            default:
                break;
        }
        workspace.setSystem(true);
        return workspace;
    }

    /**
     * Creates a {@code Repository} instance based on the provided {@code RepositoryTemplate}
     * and associates it with a specific user. The method ensures that all necessary parameters
     * for the repository type are configured using default values or user-specific settings.
     *
     * @param template the template used to create the repository; must not be null.
     * @param userId the unique identifier of the user for whom the repository is being created; must not be null.
     * @return a newly created {@code Repository} instance configured according to the specified template and user-specific settings.
     * @throws TopologyException if required configuration values for the repository type are missing.
     */
    public static Repository createFromTemplate(RepositoryTemplate template, String userId) {
        final Repository workspace = template.toRepository();
        workspace.setUserId(userId);
        final ConfigurationProvider cfgProvider = ConfigurationManager.getInstance();
        RepositoryType type = workspace.getType();
        switch (type) {
            case LOCAL:
                workspace.getParameters().replace(type.rootKey(),
                                                  cfgProvider.getValue("workspace.location") + "/" + userId);
                break;
            case AWS:
            case SWIFT:
                final HashMap<String, String> parameters = workspace.getParameters();
                final Map<String, Boolean> typeParameters = type.getParameters();
                String value;
                for (String key : parameters.keySet()) {
                    if (type.rootKey().equals(key)) {
                        value = cfgProvider.getValue(type.rootKey());
                        if (value != null) {
                            value += "/" + userId;
                        }
                    } else {
                        value = cfgProvider.getValue(key);
                    }
                    if (value == null && typeParameters.get(key)) {
                        throw new TopologyException("Value [" + key + "] for default workspace of type " + type.name() + " not configured");
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
