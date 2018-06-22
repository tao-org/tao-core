/*
 * Copyright (C) 2017 CS ROMANIA
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.UserRepository;
import ro.cs.tao.user.User;
import ro.cs.tao.user.UserPreference;
import ro.cs.tao.user.UserStatus;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("userManager")
public class UserManager {

    private Logger logger = Logger.getLogger(UserManager.class.getName());

    /** CRUD Repository for User entities */
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;

    //region User
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

    @Transactional(readOnly = true)
    public List<UserPreference> getUserPreferences(String userName) throws PersistenceException {
        final User user = userRepository.findByUsername(userName);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(userName));
        }
        return user.getPreferences();
    }

    @Transactional(readOnly = true)
    public String getUserOrganization(String userName) throws PersistenceException {
        final User user = userRepository.findByUsername(userName);
        if (user == null)
        {
            throw new PersistenceException("There is no user with the given username: " + String.valueOf(userName));
        }
        return user.getOrganization();
    }

    @Transactional(readOnly = true)
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
    public User updateUser(User updatedInfo) throws PersistenceException {
        User user = userRepository.findByUsername(updatedInfo.getUsername());
        if (user != null && user.getUsername().equalsIgnoreCase(updatedInfo.getUsername()) &&
          user.getId() == updatedInfo.getId())
        {
            // copy updated info (it's dangerous to save whatever received)
            transferUpdates(user, updatedInfo);
            user = userRepository.save(user);
        }
        else {
            throw new PersistenceException("Inconsistent updated info received for user: " + String.valueOf(updatedInfo.getUsername()));
        }
        return user;
    }

    private void transferUpdates(User original, User updated) throws PersistenceException {
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

        // check if alternative email changed
        if (!original.getAlternativeEmail().equalsIgnoreCase(updated.getAlternativeEmail())) {
            // there should not be another user with the same email address
            if (userRepository.findByEmail(updated.getAlternativeEmail()) != null ||
                userRepository.findByAlternativeEmail(updated.getAlternativeEmail()) != null) {
                throw new PersistenceException("Cannot update alternative email address for user: " + String.valueOf(updated.getUsername()) + " (address already taken)");
            }
            else {
                original.setAlternativeEmail(updated.getAlternativeEmail());
            }
        }

        // check if last name changed
        if (!original.getLastName().equalsIgnoreCase(updated.getLastName())) {
            original.setLastName(updated.getLastName());
        }

        // check if first name changed
        if (!original.getFirstName().equalsIgnoreCase(updated.getFirstName())) {
            original.setFirstName(updated.getFirstName());
        }

        // check if phone changed
        if (!original.getPhone().equalsIgnoreCase(updated.getPhone())) {
            original.setPhone(updated.getPhone());
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
        if (user.getStatus().value() != UserStatus.PENDING.value()) {
            throw new PersistenceException("Cannot activate user: " + String.valueOf(userName));
        }
        // activate user
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
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
        return prefs.stream().filter(p -> p.getKey().equals(key)).findAny().isPresent();
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

    //endregion
}
