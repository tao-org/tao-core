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
public interface StorageService<T> extends TAOService {
    /**
     * Creates a folder.
     *
     * @param folderRelativePath    The folder path, relative to the current workspace
     * @param userOnly              If <code>true</code>, the folder is created in the workspace of the calling user.
     *                              Otherwise, it is created in the shared workspace.
     * @return  The path in the user workspace
     * @throws  IOException if the path cannot be created
     */
    Path createFolder(String folderRelativePath, boolean userOnly) throws IOException;

    /**
     * Stores a file in the given folder, in the user workspace
     *
     * @param object            The file object
     * @param relativeFolder    The folder path, relative to the workspace of the calling user
     * @param description       A description for the file
     * @throws  Exception if the file cannot be stored
     */
    void storeUserFile(T object, String relativeFolder, String description) throws Exception;
    /**
     * Stores a file in the given folder, in the shared workspace
     *
     * @param object            The file object
     * @param relativeFolder    The folder path, relative to the shared workspace
     * @param description       A description for the file
     * @throws  Exception if the file cannot be stored
     */
    void storePublicFile(T object, String relativeFolder, String description) throws Exception;

    /**
     * Removes the file or folder with the given name.
     *
     * @param name          The name of the file or folder
     * @throws IOException if the file or folder cannot be removed
     */
    void remove(String name) throws IOException;

    /**
     * List the files uploaded in the shared workspace
     * @return  The list of shared uploaded files
     * @throws IOException if the content cannot be retrieved
     */
    List<FileObject> listUploaded() throws IOException;
    /**
     * List the products and uploaded files in the shared workspace
     * @return  The list of shared products and files
     * @throws IOException if the content cannot be retrieved
     */
    List<FileObject> listPublicWorkspace() throws IOException;

    /**
     * List the files uploaded in a user workspace
     * @return  The list of user uploaded files
     * @throws IOException if the content cannot be retrieved
     */
    List<FileObject> listUploaded(String userName) throws IOException;
    /**
     * List the products and the uploaded files in a user workspace
     * @param userName The user name
     * @return The list uf user products and files
     * @throws IOException if the content cannot be retrieved
     */
    List<FileObject> listUserWorkspace(String userName) throws IOException;

    /**
     * List the children files starting from the given path.
     *
     * @param fromPath  The path to look into.
     * @return The subtree of the given path
     * @throws IOException if the content cannot be retrieved
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
