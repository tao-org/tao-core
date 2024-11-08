/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package ro.cs.tao.persistence.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.UserProvider;
import ro.cs.tao.persistence.repository.GroupRepository;
import ro.cs.tao.persistence.repository.RepositoryJPARepository;
import ro.cs.tao.persistence.repository.UserRepository;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.user.*;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.workspaces.Repository;
import ro.cs.tao.workspaces.RepositoryFactory;
import ro.cs.tao.workspaces.RepositoryType;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("userManager")
public class UserManager extends EntityManager<User, String, UserRepository> implements UserProvider {

    /** CRUD Repository for Group entities */
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private RepositoryJPARepository repositoryJPARepository;

    @Override
    public boolean login(String user, String password) {
        return repository.login(user, password) != null;
    }

    @Override
    public User save(final User newUserInfo) throws PersistenceException {
        if (newUserInfo == null) {
            throw new PersistenceException("Invalid new user info received!");
        }
        final List<Group> groups = newUserInfo.getGroups();
        if (StringUtilities.isNullOrEmpty(newUserInfo.getUsername()) ||
                StringUtilities.isNullOrEmpty(newUserInfo.getEmail()) ||
                StringUtilities.isNullOrEmpty(newUserInfo.getLastName()) ||
                StringUtilities.isNullOrEmpty(newUserInfo.getFirstName()) ||
                StringUtilities.isNullOrEmpty(newUserInfo.getOrganization()) ||
                groups == null || groups.isEmpty()) {
            throw new PersistenceException("Invalid new user info received!");
        }

        // check username and email uniqueness
        if (repository.findByUsername(newUserInfo.getUsername()) != null) {
            throw new PersistenceException("Username " + newUserInfo.getUsername() + " already taken!");
        }

        // check email in alternative email addresses too
        /*if (repository.findByEmail(newUserInfo.getEmail()) != null ||
                repository.findByAlternativeEmail(newUserInfo.getEmail()) != null) {
            throw new PersistenceException("Email " + newUserInfo.getEmail() + " already taken!");
        }*/

        // check also alternative email
        /*if (!StringUtilities.isNullOrEmpty(newUserInfo.getAlternativeEmail())) {
            if (repository.findByEmail(newUserInfo.getAlternativeEmail()) != null ||
                    repository.findByAlternativeEmail(newUserInfo.getAlternativeEmail()) != null) {
                throw new PersistenceException("Email " + newUserInfo.getEmail() + " already taken!");
            }
        }*/

        // create new User entity
        User user = new User();
        user.setId(newUserInfo.getId() != null ? newUserInfo.getId() : UUID.randomUUID().toString());
        user.setUsername(newUserInfo.getUsername());
        user.setEmail(newUserInfo.getEmail());
        if (!StringUtilities.isNullOrEmpty(newUserInfo.getAlternativeEmail())) {
            user.setAlternativeEmail(newUserInfo.getAlternativeEmail());
        }
        user.setLastName(newUserInfo.getLastName());
        user.setFirstName(newUserInfo.getFirstName());
        if (!StringUtilities.isNullOrEmpty(newUserInfo.getPhone())) {
            user.setPhone(newUserInfo.getPhone());
        }
        
        if (newUserInfo.getInputQuota() == 0) {
            final int inputQuota = groups.stream().map(Group::getInputQuota).min(Comparator.comparing(Integer::intValue)).orElse(-1);
            user.setInputQuota(inputQuota);
        } else {
        	user.setInputQuota(newUserInfo.getInputQuota());
        }
        
        if (newUserInfo.getProcessingQuota() == 0) {
        	final int processingQuota = groups.stream().map(Group::getProcessingQuota).min(Comparator.comparing(Integer::intValue)).orElse(-1);
            user.setProcessingQuota(processingQuota);
        } else {
        	user.setProcessingQuota(newUserInfo.getProcessingQuota());
        }
        
        user.setCpuQuota(newUserInfo.getCpuQuota());
        user.setMemoryQuota(newUserInfo.getMemoryQuota());
        
        user.setOrganization(newUserInfo.getOrganization());
        user.setGroups(groups);
        user.setUserType(newUserInfo.getUserType());
        if (user.getUserType() != UserType.INTERNAL) {
            // external users don't need pending activation
            user.setStatus(UserStatus.ACTIVE);
        }
        else {
            user.setStatus(UserStatus.PENDING);
        }
        user.setPassword(newUserInfo.getPassword());

        // save the new user and return it
        user = repository.save(user);
        //createWorkspaces(user);
        return user;
    }

    @Override
    public List<User> list(UserStatus userStatus) {
        final List<User> users = repository.findByStatus(userStatus);
        if (SystemPrincipal.instance().getName() == null) {
            SystemPrincipal.refresh(this);
        }
        final String userId = SystemPrincipal.instance().getName();
        users.removeIf(u -> userId.equals(u.getId()));
        return users;
    }

    @Override
    public Map<String, String[]> listUnicityInfo() {
        Map<String, String[]> results = new HashMap<>();
        repository.findAll().forEach(user -> results.put(user.getUsername(), new String[] {user.getEmail(), user.getAlternativeEmail()}));
        return results;
    }

    @Override
    public Map<String, String> listNames() {
        final Map<String, String> results = new HashMap<>();
        repository.findAll().forEach(user -> results.put(user.getId(), user.getUsername()));
        return results;
    }

    @Override
    public User getByName(final String username) {
        return repository.findByUsername(username);
    }

    @Override
    public List<User> listAdministrators() {
        final List<User> administrators = repository.getAdministrators();
        administrators.removeIf(a -> SystemPrincipal.instance().getName().equals(a.getUsername()));
        return administrators;
    }

    @Override
    public List<User> listUsers(Set<String> userIds) {
        return repository.getUsers(userIds);
    }

    @Override
    public List<UserPreference> listPreferences(String userId) {
        final User user = repository.findById(userId).orElse(null);
        return user != null ? user.getPreferences() : null;
    }

    @Override
    public User update(User updatedInfo, boolean fromAdmin) throws PersistenceException {
        User user = repository.findByUsername(updatedInfo.getUsername());
        if (user != null && user.getUsername().equalsIgnoreCase(updatedInfo.getUsername()) &&
                Objects.equals(user.getId(), updatedInfo.getId())) {
            // copy updated info (it's dangerous to save whatever received)
            transferUpdates(user, updatedInfo, fromAdmin);
            // update the modified date on user
            user.setModified(LocalDateTime.now(Clock.systemUTC()));
            user = repository.save(user);
        }
        else {
            throw new PersistenceException("Inconsistent updated info received for user: " + String.valueOf(updatedInfo.getUsername()));
        }
        return user;
    }

    @Override
    public long count() {
        return repository.count();
    }

    private void transferUpdates(User original, User updated, boolean fromAdmin) throws PersistenceException {
        if (original == null || updated == null) {
            return;
        }
        if (original.getId() == null || original.getUsername().isEmpty()) {
            return;
        }
        if (!original.getUsername().equalsIgnoreCase(updated.getUsername())) {
            return;
        }

        // check if email changed
        if (!original.getEmail().equalsIgnoreCase(updated.getEmail())) {
            // there should not be another user with the same email address
            if (repository.findByEmail(updated.getEmail()) != null || repository.findByAlternativeEmail(updated.getEmail()) != null) {
                throw new PersistenceException("Cannot update email address for user: " + String.valueOf(updated.getUsername()) + " (address already taken)");
            } else {
                original.setEmail(updated.getEmail());
            }
        }

        // check if alternative email changed (with another address)
        if (!StringUtilities.isNullOrEmpty(updated.getAlternativeEmail()) &&
            (StringUtilities.isNullOrEmpty(original.getAlternativeEmail()) || !original.getAlternativeEmail().equalsIgnoreCase(updated.getAlternativeEmail()))) {
            // there should not be another user with the same email address
            if (repository.findByEmail(updated.getAlternativeEmail()) != null || repository.findByAlternativeEmail(updated.getAlternativeEmail()) != null) {
                throw new PersistenceException("Cannot update alternative email address for user: " + String.valueOf(updated.getUsername()) + " (address already taken)");
            } else {
                original.setAlternativeEmail(updated.getAlternativeEmail());
            }
        }
        // check if alternative email erased
        if (StringUtilities.isNullOrEmpty(updated.getAlternativeEmail()) && !StringUtilities.isNullOrEmpty(original.getAlternativeEmail())) {
            original.setAlternativeEmail(null);
        }

        // check if last name changed
        if (!original.getLastName().equalsIgnoreCase(updated.getLastName())) {
            original.setLastName(updated.getLastName());
        }

        // check if first name changed
        if (!original.getFirstName().equalsIgnoreCase(updated.getFirstName())) {
            original.setFirstName(updated.getFirstName());
        }

        // update phone
        original.setPhone(updated.getPhone());

        // password reset key
        if (StringUtilities.isNullOrEmpty(original.getPasswordResetKey()) ||
            !original.getPasswordResetKey().equalsIgnoreCase(updated.getPasswordResetKey())) {
            original.setPasswordResetKey(updated.getPasswordResetKey());
        }

        if (fromAdmin) {
            // check quota
            if (original.getInputQuota() != updated.getInputQuota()) {
                original.setInputQuota(updated.getInputQuota());
            }
            if (original.getProcessingQuota() != updated.getProcessingQuota()) {
                original.setProcessingQuota(updated.getProcessingQuota());
            }
            if (original.getCpuQuota() != updated.getCpuQuota()) {
                original.setCpuQuota(updated.getCpuQuota());
            }
            if (original.getMemoryQuota() != updated.getMemoryQuota()) {
                original.setMemoryQuota(updated.getMemoryQuota());
            }
            
            // check organisation
            if (!original.getOrganization().equalsIgnoreCase(updated.getOrganization())) {
                original.setOrganization(updated.getOrganization());
            }
            // check groups
            final boolean oldGroupsIncludedInNewGroups = updated.getGroups().containsAll(original.getGroups());
            final boolean newGroupsIncludedInOldGroups = original.getGroups().containsAll(updated.getGroups());
            if (!oldGroupsIncludedInNewGroups || !newGroupsIncludedInOldGroups) {
                // groups differ
                original.setGroups(updated.getGroups());
            }
        }
    }

    @Override
    public void updateLastLoginDate(String userId, LocalDateTime lastLoginDate) {
        final Optional<User> user = repository.findById(userId);
        if (user.isPresent()) {
            final User userEnt = user.get();
            userEnt.setLastLoginDate(lastLoginDate);
            repository.save(userEnt);
        }
    }

    @Override
    public void activate(String userId) throws PersistenceException {
        final User user = repository.findById(userId).orElse(null);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given id: " + userId);
        }
        // only a pending activation user can be activated
        if (!Objects.equals(user.getStatus().value(), UserStatus.PENDING.value())) {
            throw new PersistenceException("Cannot activate user: " + userId);
        }
        // activate user
        user.setStatus(UserStatus.ACTIVE);
        repository.save(user);
    }

    @Override
    public void resetPassword(String userId, String resetKey, String newPassword) throws PersistenceException {
        final User user = repository.findById(userId).orElse(null);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given id: " + userId);
        }
        // only for an internal user the password can be reset
        if (user.getUserType() != UserType.INTERNAL) {
            throw new PersistenceException("Cannot handle password for external user: " + userId);
        }

        // check if reset key matches either the previously set reset key or the old password
        if (!user.getPasswordResetKey().equalsIgnoreCase(resetKey) || !user.getPassword().equals(resetKey)) {
            throw new PersistenceException("Unauthorized password reset for user: " + userId);
        }

        // check if new password different
        if (!StringUtilities.isNullOrEmpty(user.getPassword()) && user.getPassword().equalsIgnoreCase(newPassword)) {
            throw new PersistenceException("Unauthorized password reset for user: " + userId);
        }

        // set the new password
        user.setPassword(newPassword);
        // cancel the reset key
        user.setPasswordResetKey(null);
        // save the changes
        repository.save(user);
    }

    @Override
    public void disable(String userId) throws PersistenceException {
        final User user = repository.findById(userId).orElse(null);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + userId);
        }
        // only a pending activation or an active user can be disabled
        if (!Objects.equals(user.getStatus().value(), UserStatus.PENDING.value()) &&
                !Objects.equals(user.getStatus().value(), UserStatus.ACTIVE.value())) {
            throw new PersistenceException("Cannot disable user: " + userId);
        }
        // activate user
        user.setStatus(UserStatus.DISABLED);
        repository.save(user);
    }

    @Override
    public void delete(String userId) throws PersistenceException {
        final User user = repository.findById(userId).orElse(null);
        if (user == null) {
            throw new PersistenceException("There is no user with the given id: " + userId);
        }
        // check if cascade delete or not (?)
        repository.delete(user);
    }

    @Override
    public List<UserPreference> save(String userId, List<UserPreference> newUserPreferences) throws PersistenceException {
        User user = repository.findById(userId).orElse(null);
        if (user == null) {
            throw new PersistenceException("There is no user with the given id: " + userId);
        }

        for (UserPreference newUserPref : newUserPreferences) {
            if (contains(user.getPreferences(), newUserPref.getKey())) {
                // update its value
                updatePreference(user.getPreferences(), newUserPref.getKey(), newUserPref.getValue());
            } else {
                // add it to user prefs
                user.getPreferences().add(newUserPref);
            }
        }
        user  = repository.save(user);
        return user.getPreferences();
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkEntity(User entity) {
        return entity != null && entity.getUsername() != null && entity.getGroups() != null && !entity.getGroups().isEmpty();
    }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && (existingEntity == (get(entityId) != null));
    }

    private boolean contains(List<UserPreference> prefs, String key) {
        return prefs.stream().anyMatch(p -> p.getKey().equals(key));
    }

    private void updatePreference(List<UserPreference> prefs, String key, String newValue) {
        prefs.stream().filter(p -> p.getKey().equals(key)).findFirst().get().setValue(newValue);
    }

    @Override
    public List<UserPreference> remove(String userId, List<String> userPrefsKeysToDelete) throws PersistenceException {
        User user = repository.findById(userId).orElse(null);
        if (user == null) {
            throw new PersistenceException("There is no user with the given id: " + userId);
        }
        if (userPrefsKeysToDelete == null || userPrefsKeysToDelete.isEmpty()) {
            user.setPreferences(null);
        } else {
            for (String prefKey : userPrefsKeysToDelete) {
                user.getPreferences().removeIf(entry -> entry.getKey().equals(prefKey));
            }
        }
        user  = repository.save(user);
        return user.getPreferences();
    }

    @Override
    public List<Group> listGroups() {
        final List<Group> results = new ArrayList<>();
        groupRepository.findAll().forEach(results::add);
        return results;
    }

    // Quota related methods
    /**
     * Return the user's quotas. 
     * 
     * <p>
     * The result is an array with four long values, with the following significance:
     * <ol>
     * <li> the user's input quota.
     * <li> the user's actual input quota
     * <li> the user's processing quota.
     * <li> the user's actual processing quota
     * </ol>
     * <p>
     * Users that have no quota limitation will have the value -1 in all of those fields.
     * 
     * @param userId the identifier of the user's whose quota must be obtained
     * @return an array with the four quota values.
     */
    @Override
    public long[] listDiskQuotas(String userId) {
        long[] result = null;
        // get the user
    	final User user = repository.findById(userId).orElse(null);
        if (user != null) {
            result = new long[4];
            // fill the result with data
            result[0] = user.getInputQuota();
            result[1] = user.getActualInputQuota();
            result[2] = user.getProcessingQuota();
            result[3] = user.getActualProcessingQuota();
        }
        return result;
    }
    
    /**
     * Update the user's actual input quota with the new value.
     * 
     * <p>
     *   If the current value for the actual input quota is -1, no update is performed.
     * 
     * @param userId the identifier of the user's whose quota must be updated
     * @param actualInputQuota the new value for the actual input quota
     * @return the new value of the actual input quota for the user
     * @throws PersistenceException if the user is not defined in the database
     */
    @Override
    public int updateInputQuota(String userId, int actualInputQuota) throws PersistenceException {
    	// get the user
    	final User user = repository.findById(userId).orElse(null);
        if (user == null) {
            throw new PersistenceException("There is no user with the given username: " + userId);
        }
        // check if the field should be updated to the new value or not
        if (user.getActualInputQuota() == -1) {
        	// no update is necessary as the user has no quota limitation
        	return user.getActualInputQuota();
        }
        
        // update the database entry
        user.setActualInputQuota(actualInputQuota);
        final User newUser = repository.save(user);
        
        // return the new input quota
    	return newUser.getActualInputQuota();
    }
    
    /**
     * Update the user's actual processing quota with the new value.
     * 
     * <p>
     *   If the current value for the actual processing quota is -1, no update is performed.
     * 
     * @param userId the identifier of the user's whose quota must be updated
     * @param actualProcessingQuota the new value for the actual processing quota
     * @return the new value of the actual processing quota for the user
     * @throws PersistenceException if the user is not defined in the database
     */
    @Override
    public int updateProcessingQuota(String userId, int actualProcessingQuota) throws PersistenceException {
    	// get the user
    	final User user = repository.findById(userId).orElse(null);
        if (user == null) {
            throw new PersistenceException("There is no user with the given id: " + userId);
        }
        
        // check if the field should be updated to the new value or not
        if (user.getActualProcessingQuota() == -1) {
        	// no update is necessary as the user has no quota limitation
        	return user.getActualProcessingQuota();
        }
        
        // update the database entry
        user.setActualProcessingQuota(actualProcessingQuota);
        final User newUser = repository.save(user);
        
        // return the new input quota
    	return newUser.getActualProcessingQuota();
    }
    // End Quota related methods

    @Override
    public void createWorkspaces(User user, Consumer<Repository> rootFunctor) {
        final ConfigurationProvider cfgProvider = ConfigurationManager.getInstance();
        List<RepositoryType> repoTypes =  Arrays.stream(cfgProvider
                                                              .getValue("workspaces.default", "local")
                                                              .toUpperCase()
                                                              .split(","))
                                                .map(RepositoryType::valueOf).collect(Collectors.toList());
        Repository repo;
        final List<UserPreference> preferences = user.getPreferences();
        Map<String, String> dbParams = null;
        for (RepositoryType type : repoTypes) {
            try {
                repo = repositoryJPARepository.getByUserAndName(user.getId(), type.name());
                if (repo == null) {
                    if (preferences != null && !preferences.isEmpty()) {
                        dbParams = preferences.stream()
                                              .filter(p -> p.getKey().startsWith("workspace." + type.name().toLowerCase()))
                                              .collect(Collectors.toMap(p -> p.getKey().replace("workspace.", ""),
                                                                        UserPreference::getValue));
                    }
                    final boolean useUserName = cfgProvider.getBooleanValue("aws.use.username.as.root");
                    Repository repository = useUserName
                                            ? RepositoryFactory.createDefault(type, user.getId(), user.getUsername(), dbParams)
                                            : RepositoryFactory.createDefault(type, user.getId(), dbParams);
                    repository.setPersistentStorage(type.name().equalsIgnoreCase(cfgProvider.getValue("workspaces.default.persistent", "local")));
                    repositoryJPARepository.save(repository);
                    if (rootFunctor != null) {
                        rootFunctor.accept(repository);
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(UserManager.class.getName()).warning(e.getMessage());
            }
        }
    }

    @Override
    public void createWorkspaces(String userId, Consumer<Repository> rootFunctor) {
        List<RepositoryType> repoTypes =  Arrays.stream(ConfigurationManager.getInstance()
                                                                            .getValue("workspaces.default", "local")
                                                                            .toUpperCase()
                                                                            .split(","))
                                                .map(RepositoryType::valueOf).collect(Collectors.toList());
        Repository repo;
        for (RepositoryType type : repoTypes) {
            try {
                repo = repositoryJPARepository.getByUserAndName(userId, type.name());
                if (repo == null) {
                    Repository repository = RepositoryFactory.createDefault(type, userId, null);
                    repositoryJPARepository.save(repository);
                    if (rootFunctor != null) {
                        rootFunctor.accept(repository);
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(UserManager.class.getName()).warning(e.getMessage());
            }
        }
    }

    @Override
    public String getId(String userName) {
        return repository.getId(userName);
    }

    //endregion
}
