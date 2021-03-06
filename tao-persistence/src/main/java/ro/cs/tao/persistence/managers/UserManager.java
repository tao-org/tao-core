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
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.GroupRepository;
import ro.cs.tao.persistence.repository.UserRepository;
import ro.cs.tao.user.Group;
import ro.cs.tao.user.User;
import ro.cs.tao.user.UserPreference;
import ro.cs.tao.user.UserStatus;
import ro.cs.tao.utils.StringUtilities;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("userManager")
public class UserManager {

    private Logger logger = Logger.getLogger(UserManager.class.getName());

    /** CRUD Repository for User entities */
    @Autowired
    private UserRepository userRepository;

    /** CRUD Repository for Group entities */
    @Autowired
    private GroupRepository groupRepository;

    /*@Autowired
    private DataSource dataSource;*/

    //region User

    public boolean login(String user, String password) {
        return userRepository.login(user, password) != null;
    }

    @Transactional
    public User addNewUser(final User newUserInfo) throws PersistenceException {
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
        if (userRepository.findByUsername(newUserInfo.getUsername()) != null) {
            throw new PersistenceException("Username " + newUserInfo.getUsername() + " already taken!");
        }

        // check email in alternative email addresses too
        if (userRepository.findByEmail(newUserInfo.getEmail()) != null ||
          userRepository.findByAlternativeEmail(newUserInfo.getEmail()) != null) {
            throw new PersistenceException("Email " + newUserInfo.getEmail() + " already taken!");
        }

        // check also alternative email
        if (!StringUtilities.isNullOrEmpty(newUserInfo.getAlternativeEmail())) {
            if (userRepository.findByEmail(newUserInfo.getAlternativeEmail()) != null ||
              userRepository.findByAlternativeEmail(newUserInfo.getAlternativeEmail()) != null) {
                throw new PersistenceException("Email " + newUserInfo.getEmail() + " already taken!");
            }
        }

        // create new User entity
        final User user = new User();
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
        user.setExternal(newUserInfo.isExternal());
        if (user.isExternal()) {
            // external users don't need pending activation
            user.setStatus(UserStatus.ACTIVE);
        }
        else {
            user.setStatus(UserStatus.PENDING);
        }

        // save the new user and return it
        return userRepository.save(user);
    }

    @Transactional
    public List<User> findUsersByStatus(UserStatus userStatus) {
        return userRepository.findByStatus(userStatus);
    }

    @Transactional
    public Map<String, String[]> getAllUsersUnicityInfo() {
        Map<String, String[]> results = new HashMap<>();
        userRepository.findAll().forEach(user -> results.put(user.getUsername(), new String[] {user.getEmail(), user.getAlternativeEmail()}));
        return results;
    }

    @Transactional
    public User findUserByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    /*public Map<String, String> getUserPreferences(String userName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return new HashMap<>(jdbcTemplate.query(con -> {
            PreparedStatement statement =
                    con.prepareStatement("SELECT up.pref_key, up.pref_value FROM tao.user_prefs up " +
                                                 "JOIN tao.user u ON up.user_id = u.id " +
                                                 "WHERE u.username = ?");
            statement.setString(1, userName);
            return statement;
        }, (rs, rowNum) -> {
            return new UserPreference(rs.getString(1), rs.getString(2));
        }).stream().collect(Collectors.toMap(UserPreference::getKey, UserPreference::getValue)));
    }*/

    public List<User> getAdministrators() {
        return userRepository.getAdministrators();
    }

    @Transactional
    public List<UserPreference> getUserPreferences(String userName) throws PersistenceException {
        final User user = userRepository.findByUsername(userName);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(userName));
        }
        return user.getPreferences();
    }

    @Transactional
    public String getUserOrganization(String userName) throws PersistenceException {
        final User user = userRepository.findByUsername(userName);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(userName));
        }
        return user.getOrganization();
    }

    @Transactional
    public List<Group> getUserGroups(String userName) {
        final User user = userRepository.findByUsername(userName);
        if (user != null)
        {
            return user.getGroups();
        }
        return null;
    }

    @Transactional
    public boolean checkLoginCredentials(String userName, String password) {
        final User user = userRepository.findByUsername(userName);
        if (user == null)
        {
            // no such user exists
            return false;
        }
        return user.getPassword().equals(password);
    }

    @Transactional
    public User updateUser(User updatedInfo, boolean fromAdmin) throws PersistenceException {
        User user = userRepository.findByUsername(updatedInfo.getUsername());
        if (user != null && user.getUsername().equalsIgnoreCase(updatedInfo.getUsername()) &&
                Objects.equals(user.getId(), updatedInfo.getId()))
        {
            // copy updated info (it's dangerous to save whatever received)
            transferUpdates(user, updatedInfo, fromAdmin);
            // update the modified date on user
            user.setModified(LocalDateTime.now(Clock.systemUTC()));
            user = userRepository.save(user);
        }
        else {
            throw new PersistenceException("Inconsistent updated info received for user: " + String.valueOf(updatedInfo.getUsername()));
        }
        return user;
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
            if (userRepository.findByEmail(updated.getEmail()) != null ||
                userRepository.findByAlternativeEmail(updated.getEmail()) != null) {
                throw new PersistenceException("Cannot update email address for user: " + String.valueOf(updated.getUsername()) + " (address already taken)");
            }
            else {
                original.setEmail(updated.getEmail());
            }
        }

        // check if alternative email changed (with another address)
        if (!StringUtilities.isNullOrEmpty(updated.getAlternativeEmail()) &&
            (StringUtilities.isNullOrEmpty(original.getAlternativeEmail()) || !original.getAlternativeEmail().equalsIgnoreCase(updated.getAlternativeEmail()))) {
            // there should not be another user with the same email address
            if (userRepository.findByEmail(updated.getAlternativeEmail()) != null ||
                userRepository.findByAlternativeEmail(updated.getAlternativeEmail()) != null) {
                throw new PersistenceException("Cannot update alternative email address for user: " + String.valueOf(updated.getUsername()) + " (address already taken)");
            }
            else {
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

    @Transactional
    public void updateUserLastLoginDate(Long userId, LocalDateTime lastLoginDate) {
        final Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            final User userEnt = user.get();
            userEnt.setLastLoginDate(lastLoginDate);
            userRepository.save(userEnt);
        }
    }

    @Transactional
    public void activateUser(String userName) throws PersistenceException {
        final User user = userRepository.findByUsername(userName);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(userName));
        }
        // only a pending activation user can be activated
        if (!Objects.equals(user.getStatus().value(), UserStatus.PENDING.value())) {
            throw new PersistenceException("Cannot activate user: " + String.valueOf(userName));
        }
        // activate user
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void resetUserPassword(String userName, String resetKey, String newPassword) throws PersistenceException {
        final User user = userRepository.findByUsername(userName);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(userName));
        }
        // only for an internal user the password can be reset
        if (user.isExternal()) {
            throw new PersistenceException("Cannot handle password for external user: " + String.valueOf(userName));
        }

        // check if reset key matches
        if (!user.getPasswordResetKey().equalsIgnoreCase(resetKey)) {
            throw new PersistenceException("Unauthorized password reset for user: " + String.valueOf(userName));
        }

        // check if new password different
        if (!StringUtilities.isNullOrEmpty(user.getPassword()) && user.getPassword().equalsIgnoreCase(newPassword)) {
            throw new PersistenceException("Unauthorized password reset for user: " + String.valueOf(userName));
        }

        // set the new password
        user.setPassword(newPassword);
        // cancel the reset key
        user.setPasswordResetKey(null);
        // save the changes
        userRepository.save(user);
    }

    @Transactional
    public void disableUser(String userName) throws PersistenceException {
        final User user = userRepository.findByUsername(userName);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(userName));
        }
        // only a pending activation or an active user can be disabled
        if (!Objects.equals(user.getStatus().value(), UserStatus.PENDING.value()) &&
                !Objects.equals(user.getStatus().value(), UserStatus.ACTIVE.value())) {
            throw new PersistenceException("Cannot disable user: " + String.valueOf(userName));
        }
        // activate user
        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userName) throws PersistenceException {
        final User user = userRepository.findByUsername(userName);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(userName));
        }
        // check if cascade delete or not (?)
        userRepository.delete(user);
    }

    @Transactional
    public List<UserPreference> saveOrUpdateUserPreferences(String username, List<UserPreference> newUserPreferences) throws PersistenceException {
        User user = userRepository.findByUsername(username);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(username));
        }

        for (UserPreference newUserPref : newUserPreferences) {
            if (contains(user.getPreferences(), newUserPref.getKey())) {
                // update its value
                updatePreference(user.getPreferences(), newUserPref.getKey(), newUserPref.getValue());
            }
            else {
                // add it to user prefs
                user.getPreferences().add(newUserPref);
            }
        }

        user  = userRepository.save(user);
        return user.getPreferences();
    }

    private boolean contains(List<UserPreference> prefs, String key) {
        return prefs.stream().anyMatch(p -> p.getKey().equals(key));
    }

    private void updatePreference(List<UserPreference> prefs, String key, String newValue) {
        prefs.stream().filter(p -> p.getKey().equals(key)).findFirst().get().setValue(newValue);
    }

    @Transactional
    public List<UserPreference> removeUserPreferences(String username, List<String> userPrefsKeysToDelete) throws PersistenceException {
        User user = userRepository.findByUsername(username);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(username));
        }

        for (String prefKey : userPrefsKeysToDelete) {
            user.getPreferences().removeIf(entry -> entry.getKey().equals(prefKey));
        }

        user  = userRepository.save(user);
        return user.getPreferences();
    }

    @Transactional
    public List<Group> getGroups() {
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
     * 
     * <p>
     * Users that have no quota limitation will have the value -1 in all of those fields.
     * 
     * @param username the identifier of the user's whose quota must be obtained
     * @return an array with the four quota values.
     * @throws PersistenceException if the user is not defined in the database
     */
    @Transactional
    public long[] getUserDiskQuotas(String username) throws PersistenceException {
        
    	// get the user
    	final User user = userRepository.findByUsername(username);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(username));
        }
        
        final long result[] = new long[4];
        // fill the result with data 
        result[0] = user.getInputQuota();
        result[1] = user.getActualInputQuota();
        result[2] = user.getProcessingQuota();
        result[3] = user.getActualProcessingQuota();
        
        return result;
    }
    
    /**
     * Update the user's actual input quota with the new value.
     * 
     * <p>
     *   If the current value for the actual input quota is -1, no update is performed.
     * 
     * @param username the identifier of the user's whose quota must be updated
     * @param actualInputQuota the new value for the actual input quota
     * @return the new value of the actual input quota for the user
     * @throws PersistenceException if the user is not defined in the database
     */
    @Transactional
    public int updateUserInputQuota(String username, int actualInputQuota) throws PersistenceException {
    	// get the user
    	final User user = userRepository.findByUsername(username);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(username));
        }
        
        // check if the field should be updated to the new value or not
        if (user.getActualInputQuota() == -1)
        {
        	// no update is necessary as the user has no quota limitation
        	return user.getActualInputQuota();
        }
        
        // update the database entry
        user.setActualInputQuota(actualInputQuota);
        final User newUser = userRepository.save(user);
        
        // return the new input quota
    	return newUser.getActualInputQuota();
    }
    
    /**
     * Update the user's actual processing quota with the new value.
     * 
     * <p>
     *   If the current value for the actual processing quota is -1, no update is performed.
     * 
     * @param username the identifier of the user's whose quota must be updated
     * @param actualProcessingQuota the new value for the actual processing quota
     * @return the new value of the actual processing quota for the user
     * @throws PersistenceException if the user is not defined in the database
     */
    @Transactional
    public int updateUserProcessingQuota(String username, int actualProcessingQuota) throws PersistenceException {
    	// get the user
    	final User user = userRepository.findByUsername(username);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(username));
        }
        
        // check if the field should be updated to the new value or not
        if (user.getActualProcessingQuota() == -1)
        {
        	// no update is necessary as the user has no quota limitation
        	return user.getActualProcessingQuota();
        }
        
        // update the database entry
        user.setActualProcessingQuota(actualProcessingQuota);
        final User newUser = userRepository.save(user);
        
        // return the new input quota
    	return newUser.getActualProcessingQuota();
    }
    // End Quota related methods
    //endregion
}
