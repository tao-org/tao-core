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
package ro.cs.tao.services.interfaces;

import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.services.model.user.UserUnicityInfo;
import ro.cs.tao.user.Group;
import ro.cs.tao.user.User;
import ro.cs.tao.user.UserStatus;

import java.util.List;

/**
 * Service for managing administration tasks.
 *
 * @author Oana H.
 */
public interface AdministrationService {

    User addNewUser(User userInfo) throws PersistenceException;

    List<UserUnicityInfo> getAllUsersUnicityInfo();

    List<User> findUsersByStatus(UserStatus activationStatus);

    List<Group> getGroups();

    User getUserInfo(String username);

    User updateUserInfo(User updatedInfo) throws PersistenceException;

    void disableUser(String username) throws PersistenceException;

    void deleteUser(String username) throws PersistenceException;
}
