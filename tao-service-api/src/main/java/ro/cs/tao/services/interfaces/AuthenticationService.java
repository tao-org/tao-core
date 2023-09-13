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

import ro.cs.tao.security.Token;
import ro.cs.tao.services.model.auth.AuthInfo;

/**
 * Service for managing authentication.
 *
 * @author Oana H.
 */
public interface AuthenticationService extends TAOService {

    /**
     * Login user using the credentials
     *
     * @param user  User login
     * @param password User password
     * @return authentication result
     */
    AuthInfo login(String user, String password);

    /**
     * Login user the code given by the external authenticator
     *
     * @param code  Code to obtain the token
     * @return authentication result
     */
    AuthInfo loginWithCode(String code);

    /**
     * Logout user
     *
     * @param authenticationToken User authentication token
     */
    boolean logout(String authenticationToken);

    /**
     * Retrieves a new access token (if supported by the token provider).
     *
     * @param user          The user login
     * @param refreshToken  The token used to get a new access token
     */
    Token getNewToken(String user, String refreshToken);
}
