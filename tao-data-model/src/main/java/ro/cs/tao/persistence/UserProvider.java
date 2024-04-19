package ro.cs.tao.persistence;

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

public interface UserProvider extends EntityProvider<User, String> {

    boolean login(String user, String password);
    List<User> list(UserStatus status);
    List<User> listAdministrators();
    List<User> listUsers(Set<String> names);
    List<Group> listGroups();
    User getByName(String userName);
    List<UserPreference> listPreferences(String user);
    User update(User user, boolean asAdmin) throws PersistenceException;
    String getId(String userName);
    long count();
    void updateLastLoginDate(String userId, LocalDateTime lastLoginDate) throws PersistenceException;
    void activate(String userId) throws PersistenceException;
    void resetPassword(String userId, String resetKey, String newPassword) throws PersistenceException;
    void disable(String userId) throws PersistenceException;
    void delete(String userId) throws PersistenceException;
    List<UserPreference> save(String userId, List<UserPreference> preferences) throws PersistenceException;
    List<UserPreference> remove(String userId, List<String> preferences) throws PersistenceException;
    long[] listDiskQuotas(String userId);
    int updateInputQuota(String userId, int value) throws PersistenceException;
    int updateProcessingQuota(String userId, int value) throws PersistenceException;
    Map<String, String[]> listUnicityInfo();
    Map<String, String> listNames();
    default int defaultCPUQuota() { return 8; }
    default int defaultMemoryQuota() { return 16; }
    default void createWorkspaces(User user, Consumer<Repository> rootFunctor) { }
    default void createWorkspaces(String userId, Consumer<Repository> rootFunctor) { }
}
