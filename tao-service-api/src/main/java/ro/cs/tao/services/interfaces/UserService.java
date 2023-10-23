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
package ro.cs.tao.services.interfaces;

import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.services.model.user.ResetPasswordInfo;
import ro.cs.tao.user.Group;
import ro.cs.tao.user.User;
import ro.cs.tao.user.UserPreference;

import java.util.List;

/**
 * Service for managing user specific tasks.
 *
 * @author Oana H.
 */
public interface UserService extends TAOService {

    List<Group> getGroups();

    void activateUser(String userId) throws PersistenceException;

    void createWorkspaces(String userId);

    void resetPassword(String userId, ResetPasswordInfo resetInfo) throws PersistenceException;

    User getUserInfo(String userId);

    User updateUserInfo(User updatedInfo) throws PersistenceException;

    List<UserPreference> saveOrUpdateUserPreferences(String userId, List<UserPreference> userPreferences) throws PersistenceException;

    List<UserPreference> removeUserPreferences(String userId, List<String> userPrefsKeysToDelete) throws PersistenceException;
}
