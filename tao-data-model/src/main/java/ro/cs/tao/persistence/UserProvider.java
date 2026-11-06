package ro.cs.tao.persistence;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.user.Group;
import ro.cs.tao.user.User;
import ro.cs.tao.user.UserPreference;
import ro.cs.tao.user.UserStatus;
import ro.cs.tao.workspaces.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Provides methods for managing and interacting with user accounts in the system.
 * This includes user authentication, querying users based on various criteria,
 * updating user information, managing user quotas, and handling user preferences.
 * Utility methods for workspace creation and default resource limits are also provided.
 *
 * The {@code UserProvider} interface ensures a standardized way to work with user-related
 * entities, including integrating persistence features for managing users effectively.
 *
 * In addition to basic CRUD operations, it includes specialized functionalities such as:
 * - User login and authentication.
 * - Management of user preferences and quotas.
 * - Retrieval of user-specific and administrative lists.
 * - Account activation, disabling, or deletion.
 * - Password management and reset operations.
 */
public interface UserProvider extends EntityProvider<User, String> {

    /**
     * Authenticates a user using the provided credentials.
     *
     * @param user the username of the user attempting to log in
     * @param password the password associated with the given username
     * @return true if the authentication is successful, false otherwise
     */
    boolean login(String user, String password);
    /**
     * Retrieves a list of users based on their activation status.
     *
     * @param status the activation status of the users to filter by (e.g., PENDING, ACTIVE, DISABLED)
     * @return a list of users matching the specified activation status
     */
    List<User> list(UserStatus status);
    /**
     * Retrieves a list of users filtered by a set of user IDs and a specific user status.
     *
     * @param ids a set of unique user IDs to filter the results, or an empty set to include all user IDs
     * @param status the activation status of the users to filter by (e.g., PENDING, ACTIVE, DISABLED)
     * @return a list of users that match the specified criteria, or an empty list if no users are found
     */
    List<User> list(Set<String> ids, UserStatus status);
    /**
     * Retrieves a list of users who have administrator privileges.
     *
     * @return a list of User objects representing administrators
     */
    List<User> listAdministrators();
    /**
     * Retrieves a list of users whose usernames match the specified set of names.
     *
     * @param names a set of usernames to filter the results; an empty set includes all users
     * @return a list of User objects that match the specified usernames, or an empty list if no users are found
     */
    List<User> listUsers(Set<String> names);
    /**
     * Retrieves a list of groups available in the system.
     *
     * @return a list of Group objects representing the available groups, or an empty list if no groups are found
     */
    List<Group> listGroups();
    /**
     * Retrieves a user based on the provided username.
     *
     * @param userName the username of the user to retrieve
     * @return the User object corresponding to the specified username, or null if no user is found
     */
    User getByName(String userName);

    /**
     * Retrieves a User object associated with the provided API key.
     *
     * @param apiKey the API key used to identify the User
     * @return the User object corresponding to the provided API key,
     *         or null if no matching User is found
     */
    User getByApiKey(String apiKey);

    /**
     * Retrieves a list of preferences for a specific user.
     *
     * @param user the username of the user whose preferences are to be listed
     * @return a list of UserPreference objects representing the user's preferences
     */
    List<UserPreference> listPreferences(String user);

    /**
     * Retrieves the user preference based on the specified user ID and preference key.
     *
     * @param userId The unique identifier for the user whose preference is being queried.
     * @param key The key corresponding to the specific preference of the user to be retrieved.
     * @return The UserPreference object containing the preference data associated with the given user ID and key.
     */
    UserPreference getPreference(String userId, String key);
    /**
     * Updates the details of a specific user. If the operation is performed as an administrator,
     * additional permissions or actions might be applied.
     *
     * @param user the User object containing the updated user details
     * @param asAdmin a boolean flag indicating whether the update is performed with administrative privileges
     * @return the updated User object after the changes have been saved
     * @throws PersistenceException if an error occurs while persisting the user update
     */
    User update(User user, boolean asAdmin) throws PersistenceException;
    /**
     * Retrieves the unique identifier associated with the specified username.
     *
     * @param userName the username for which the unique identifier is requested
     * @return the unique identifier as a String, or null if no identifier is found for the given username
     */
    String getId(String userName);
    /**
     * Counts the total number of users in the system.
     *
     * @return the total count of users as a long value
     */
    long count();
    /**
     * Updates the last login date of a user identified by the given user ID.
     *
     * @param userId the unique identifier of the user whose last login date is to be updated
     * @param lastLoginDate the date and time of the user's last login
     * @throws PersistenceException if an error occurs while updating the last login date in the database
     */
    void updateLastLoginDate(String userId, LocalDateTime lastLoginDate) throws PersistenceException;
    /**
     * Activates a user identified by the specified user ID. This operation transitions
     * the user's status to "active" in the persistence layer.
     *
     * @param userId the unique identifier of the user to be activated
     * @throws PersistenceException if an error occurs while updating the user's status in the database
     */
    void activate(String userId) throws PersistenceException;
    /**
     * Resets the password for a user identified by the given userId.
     * This operation updates the user's password in the persistence layer,
     * provided the resetKey matches the one associated with the user.
     *
     * @param userId the unique identifier of the user whose password is to be reset
     * @param resetKey the key used to authorize the password reset operation
     * @param newPassword the new password to be set for the user
     * @throws PersistenceException if an error occurs while updating the password in the database
     */
    void resetPassword(String userId, String resetKey, String newPassword) throws PersistenceException;
    /**
     * Disables a user identified by the specified user ID. This operation transitions
     * the user's status to "disabled" in the persistence layer, potentially restricting
     * their access to the system.
     *
     * @param userId the unique identifier of the user to be disabled
     * @throws PersistenceException if an error occurs while disabling the user in the database
     */
    void disable(String userId) throws PersistenceException;
    /**
     * Deletes the user identified by the specified user ID from the persistence layer.
     *
     * @param userId the unique identifier of the user to be deleted
     * @throws PersistenceException if an error occurs while deleting the user in the database
     */
    void delete(String userId) throws PersistenceException;
    /**
     * Saves a list of user preferences for a specified user. If preferences for the user already exist,
     * this method updates them. Otherwise, new preferences are created.
     *
     * @param userId the unique identifier of the user for whom the preferences are to be saved
     * @param preferences a list of UserPreference objects representing the preferences to be saved
     * @return a list of UserPreference objects after they have been successfully saved or updated
     * @throws PersistenceException if an error occurs while persisting the preferences
     */
    List<UserPreference> save(String userId, List<UserPreference> preferences) throws PersistenceException;
    /**
     * Removes specific preferences for a user identified by the provided user ID.
     *
     * @param userId the unique identifier of the user whose preferences are to be removed
     * @param preferences a list of preference keys identifying which preferences to remove
     * @return a list of UserPreference objects representing the removed preferences
     * @throws PersistenceException if an error occurs while removing the preferences from the persistence layer
     */
    List<UserPreference> remove(String userId, List<String> preferences) throws PersistenceException;
    /**
     * Retrieves an array containing the disk quotas allocated to a user identified by the provided user ID.
     *
     * @param userId the unique identifier of the user whose disk quotas are to be listed
     * @return an array of long values representing the disk quotas for the specified user
     */
    long[] listDiskQuotas(String userId);
    /**
     * Updates the input quota for a user identified by their user ID. The new quota value will
     * replace the current quota for the user in the persistence layer.
     *
     * @param userId the unique identifier of the user whose input quota is to be updated
     * @param value the new input quota value to be set for the user
     * @return the updated input quota value as an integer
     * @throws PersistenceException if an error occurs while updating the input quota in the database
     */
    int updateInputQuota(String userId, int value) throws PersistenceException;
    /**
     * Updates the processing quota for a user identified by their user ID. The new quota value
     * will replace the current processing quota for the user in the persistence layer.
     *
     * @param userId the unique identifier of the user whose processing quota is to be updated
     * @param value the new processing quota value to be set for the user
     * @return the updated processing quota value as an integer
     * @throws PersistenceException if an error occurs while updating the processing quota in the database
     */
    int updateProcessingQuota(String userId, int value) throws PersistenceException;
    /**
     * Retrieves a map containing unicity information for various entities.
     * The keys in the map represent the entity type or category,
     * and the values are arrays of field names that must maintain unicity (unique values)
     * within that entity type.
     *
     * @return a map where the key is a string representing the entity type or category,
     *         and the value is an array of strings representing the fields that must have unique values.
     */
    Map<String, String[]> listUnicityInfo();
    /**
     * Retrieves a map of identifiers and their corresponding names.
     * The keys represent unique identifiers (e.g., user IDs or entity IDs),
     * and the values represent the associated names.
     *
     * @return a map where each key is a string representing an identifier,
     *         and each value is the name corresponding to that identifier.
     */
    Map<String, String> listNames();

    /**
     * Retrieves the default input quota for users from the configuration settings.
     * The method fetches the value associated with the key "topology.default.user.block.storage"
     * in the configuration. If no value is provided, it defaults to 10.
     *
     * @return the default input quota as an integer, representing storage size in gigabytes (GB)
     */
    default int defaultInputQuota() {
        return Integer.parseInt(ConfigurationManager.getInstance().getValue("topology.default.user.block.storage", "10"));
    }

    /**
     * Retrieves the default processing quota for the user in terms of S3 storage.
     * The value is fetched from the application's configuration settings, falling back to a default value if not specified.
     *
     * @return the default processing quota as an integer, representing storage size in gigabytes (GB)
     */
    default int defaultProcessingQuota() {
        return Integer.parseInt(ConfigurationManager.getInstance().getValue("topology.default.user.s3.storage", "50"));
    }

    /**
     * Retrieves the default memory quota for a topology user from the configuration.
     * If the configuration value is not found, it defaults to 16384.
     *
     * @return the default memory quota value as an integer
     */
    default int defaultMemoryQuota() {
        return Integer.parseInt(ConfigurationManager.getInstance().getValue("topology.default.user.memory", "16384"));
    }

    /**
     * Retrieves the default CPU quota for a topology configuration.
     * The value is fetched from a configuration property "topology.default.user.cpu"
     * and defaults to "4" if the property is not set.
     *
     * @return the default CPU quota as an integer
     */
    default int defaultCPUQuota() {
        return Integer.parseInt(ConfigurationManager.getInstance().getValue("topology.default.user.cpu", "4"));
    }
    /**
     * Creates workspaces for the specified user by applying the given function
     * to repositories.
     *
     * @param user the user for whom the workspaces are being created
     * @param rootFunctor a Consumer function that takes a Repository and performs
     *                    operations on it
     */
    default void createWorkspaces(User user, Consumer<Repository> rootFunctor) { }
    /**
     * Creates workspaces for the specified user and applies the provided
     * operation to the root repository of each workspace.
     *
     * @param userId the unique identifier of the user for whom workspaces are created
     * @param rootFunctor a consumer function that operates on the root repository of each workspace
     */
    default void createWorkspaces(String userId, Consumer<Repository> rootFunctor) { }

    /**
     * Retrieves the API key associated with the specified user.
     *
     * @param userId the unique identifier of the user whose API key is to be retrieved
     * @return the API key of the specified user, or null if no key exists for the user
     */
    String getUserApiKey(String userId);

    /**
     * Updates the API key associated with a specific user.
     *
     * @param userId The unique identifier of the user whose API key will be updated.
     * @param apiKey The new API key to associate with the user.
     * @param expiration Tge expiration datetime of the API key
     */
    void updateUserApiKey(String userId, String apiKey, LocalDateTime expiration);

    /**
     * Deletes API keys that have expired based on their expiration date.
     * This method removes expired keys from the storage or database to ensure
     * that obsolete data is cleaned up and no longer accessible for use.
     * It is intended to help maintain system security and prevent misuse of
     * expired API credentials.
     */
    void deleteExpiredApiKeys();
}
