package ro.cs.tao.persistence;

import ro.cs.tao.user.Group;
import ro.cs.tao.user.User;
import ro.cs.tao.user.UserPreference;
import ro.cs.tao.user.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserProvider extends EntityProvider<User, Long> {

    boolean login(String user, String password);
    List<User> list(UserStatus status);
    List<User> listAdministrators();
    List<User> listUsers(Set<String> names);
    List<Group> listGroups();
    User getByName(String userName);
    List<UserPreference> listPreferences(String user);
    User update(User user, boolean asAdmin) throws PersistenceException;
    void updateLastLoginDate(Long userId, LocalDateTime lastLoginDate) throws PersistenceException;
    void activate(String userName) throws PersistenceException;
    void resetPassword(String userName, String resetKey, String newPassword) throws PersistenceException;
    void disable(String userName) throws PersistenceException;
    void delete(String userName) throws PersistenceException;
    List<UserPreference> save(String user, List<UserPreference> preferences) throws PersistenceException;
    List<UserPreference> remove(String user, List<String> preferences) throws PersistenceException;
    long[] listDiskQuotas(String userName);
    int updateInputQuota(String userName, int value) throws PersistenceException;
    int updateProcessingQuota(String userName, int value) throws PersistenceException;
    Map<String, String[]> listUnicityInfo();
    default int defaultCPUQuota() { return 8; }
    default int defaultMemoryQuota() { return 16; }
    default void createWorkspaces(String user) { }
}
