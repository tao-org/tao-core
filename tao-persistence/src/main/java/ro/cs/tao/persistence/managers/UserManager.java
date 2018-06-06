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
    public boolean checkLoginCredentials(String userName, String password){
        final User user = userRepository.findByUsername(userName);
        if (user == null)
        {
            // no such user exists
            return false;
        }
        return user.getPassword().equals(password);
    }

    //endregion
}
