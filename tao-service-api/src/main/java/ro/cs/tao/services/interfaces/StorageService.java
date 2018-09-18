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

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public interface StorageService<T> extends TAOService {
    void store(T object, String description) throws Exception;
    void remove(String name) throws IOException;
    Stream<Path> listFiles(boolean userOnly) throws IOException;
    Stream<Path> listWorkspace(boolean userOnly) throws IOException;
    Stream<Path> listFiles(String fromPath) throws IOException;
}
