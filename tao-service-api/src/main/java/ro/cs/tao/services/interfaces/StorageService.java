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
import ro.cs.tao.services.model.FileObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Service interface for manipulation of files and folders (server-side).
 *
 * @author Cosmin Cara
 */
@Service
public interface StorageService<T> extends TAOService {
    /**
     * Creates a folder.
     *
     * @param folderRelativePath    The folder path, relative to the current workspace
     * @param userOnly              If <code>true</code>, the folder is created in the workspace of the calling user.
     *                              Otherwise, it is created in the shared workspace.
     */
    Path createFolder(String folderRelativePath, boolean userOnly) throws IOException;

    /**
     * Stores a file in the given folder, in the user workspace
     *
     * @param object            The file object
     * @param relativeFolder    The folder path, relative to the workspace of the calling user
     * @param description       A description for the file
     */
    void storeUserFile(T object, String relativeFolder, String description) throws Exception;
    /**
     * Stores a file in the given folder, in the shared workspace
     *
     * @param object            The file object
     * @param relativeFolder    The folder path, relative to the shared workspace
     * @param description       A description for the file
     */
    void storePublicFile(T object, String relativeFolder, String description) throws Exception;

    /**
     * Removes the file or folder with the given name.
     *
     * @param name          The name of the file or folder
     */
    void remove(String name) throws IOException;

    List<FileObject> listUploaded() throws IOException;
    /**
     * List the products and uploaded files in the shared workspace
     *
     */
    List<FileObject> listPublicWorkspace() throws IOException;

    List<FileObject> listUploaded(String userName) throws IOException;
    /**
     * List the products and the uploaded files in a user workspace
     * @param userName The user name
     */
    List<FileObject> listUserWorkspace(String userName) throws IOException;

    /**
     * List the children files starting from the given path.
     *
     * @param fromPath  The path to look into.
     */
    List<FileObject> listFiles(Path fromPath, Set<Path> exclusions) throws IOException;

    /**
     * Lists the results of the given workflow, regardless of the execution job.
     * The results come as a combination of file information and product information.
     *
     * @param workflowId    The id of the workflow.
     */
    List<FileObject> getWorkflowResults(long workflowId) throws IOException;

    /**
     * List the results of the given execution job.
     * The results come as a combination of file information and product information.
     *
     * @param jobId         The job identifier.
     */
    List<FileObject> getJobResults(long jobId) throws IOException;
}
