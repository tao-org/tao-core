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

import ro.cs.tao.persistence.AuxiliaryDataProvider;
import ro.cs.tao.persistence.EOProductProvider;
import ro.cs.tao.persistence.VectorDataProvider;
import ro.cs.tao.services.model.FileObject;
import ro.cs.tao.services.model.ItemAction;
import ro.cs.tao.utils.Crypto;
import ro.cs.tao.utils.executors.monitoring.ProgressListener;
import ro.cs.tao.workspaces.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipOutputStream;

/**
 * Service interface for manipulation of files and folders (server-side).
 *
 * @author Cosmin Cara
 */
public interface StorageService<T, U> extends TAOService {

    String FOLDER_PLACEHOLDER = ".s3keep";
    String CONTENTS_ATTRIBUTE = "contents";
    String REMOTE_PATH_ATTRIBUTE = "remotePath";
    String ROOT_TITLE = "__root";

    /**
     * Indicates if this storage service implementation is intended for the given protocol
     * @param protocol  The protocol
     */
    boolean isIntendedFor(String protocol);

    /**
     * Associates a repository configuration to this service instance
     * @param repository    The repository
     */
    default void associate(Repository repository) { }

    default void setProductProvider(EOProductProvider productProvider) { }

    default void setVectorDataProvider(VectorDataProvider vectorDataProvider) { }

    default void setAuxiliaryDataProvider(AuxiliaryDataProvider auxiliaryDataProvider) { }

    /**
     * Creates the root of the repository. Since not all storages support this, the default implementation
     * does nothing. It is up to the specializations to override this method with the proper behavior.
     *
     * @param root    The root of the repository
     * @throws  IOException if the root cannot be created
     */
    default void createRoot(String root) throws IOException { }
    /**
     * Creates a folder.
     *
     * @param relativePath    The folder path, relative to the current workspace
     * @param userOnly              If <code>true</code>, the folder is created in the workspace of the calling user.
     *                              Otherwise, it is created in the shared workspace.
     * @return  The path in the user workspace
     * @throws  IOException if the path cannot be created
     */
    Path createFolder(String relativePath, boolean userOnly) throws IOException;

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
     * Stores a file in the given folder, in the user workspace
     *
     * @param stream            The input stream of the file object
     * @param length            The file length. If unknown, pass a value less than 0
     * @param relativeFolder    The folder path, relative to the workspace of the calling user
     * @param description       A description for the file
     * @throws  Exception if the file cannot be stored
     */
    void storeFile(InputStream stream, long length, String relativeFolder, String description) throws Exception;

    /**
     * Checks if the given relative path exists in the workspace
     *
     * @param path          The path to check in the workspace
     */
    boolean exists(String path) throws Exception;

    /**
     * Removes the file or folder with the given name.
     *
     * @param path          The relative path of the file or folder
     * @throws IOException if the file or folder cannot be removed
     */
    void remove(String path) throws IOException;

    /**
     * Moves a file from a source directory to a destination in the same workspace
     *
     * @param source          The original relative path of the file
     * @param destination     The new relative destination of the file
     * @throws IOException if the file or folder cannot be moved
     */
    void move(String source, String destination) throws IOException;

    default void rename(String source, String target) throws IOException {

    }

    /**
     * List the products and the uploaded files in a user workspace
     * @return The list uf user products and files
     * @throws IOException if the content cannot be retrieved
     */
    default List<FileObject> listUserWorkspace() throws IOException {
        return listFiles("/", null, null, 1);
    }

    /**
     * List the children files starting from the given path.
     *
     * @param fromPath       The path to look into.
     * @return               The subtree of the given path
     * @throws IOException if the content cannot be retrieved
     */
    List<FileObject> listFiles(String fromPath, Set<String> exclusions, String lastItem, int depth) throws IOException;

    default List<FileObject> listFiles(String fromPath, Set<String> exclusions, String lastItem, int depth, Set<Path> excludedPaths) throws IOException {
        return listFiles(fromPath, exclusions, lastItem, depth);
    }

    /**
     * Lists the full tree of folders and files starting from the given path.
     * This method should be used with caution for remote repositories, as the full traversal may take long time.
     *
     * @param fromPath      The path to look into
     * @throws IOException if the content cannot be retrieved
     */
    List<FileObject> listTree(String fromPath) throws IOException;

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

    /**
     * Downloads the object from the given path.
     * Some implementors may not return the binary content of the object, but an InputStream to the object.
     *
     * @param path  The path of the object to be retrieved.
     *
     * @throws IOException
     */
    U download(String path) throws IOException;

    /**
     * Packs the children of an object into the given zip stream.
     *
     * @param zipRoot   The root of the subtree to be packed
     * @param stream    The zip stream (created elsewhere)
     *
     * @throws IOException
     */
    void streamToZip(String zipRoot, ZipOutputStream stream) throws IOException;

    /**
     * Reads a number of lines from the given resource.
     *
     * @param resource  The storage resource
     * @param lines     The number of lines to be read
     * @param skipLines The number of lines to be skipped before reading
     *
     * @throws IOException
     */
    default String readAsText(U resource, int lines, int skipLines) throws IOException {
        return null;
    }

    /**
     * Sets a listener for progress reporting
     */
    default void setProgressListener(ProgressListener listener) { }

    /**
     * Helper method for creating the (fake) root node of any storage tree.
     *
     * @param repository    The repository for which to create the fake root node
     */
    default FileObject repositoryRootNode(Repository repository) {
        final FileObject object = new FileObject(repository.getUrlPrefix(), "/", true, 0, ROOT_TITLE);
        object.addAttribute(REMOTE_PATH_ATTRIBUTE, repository.root());
        return object;
    }

    /**
     * Returns a placeholder "file" to be used for S3-like storages when creating a "folder"
     */
    default FileObject emptyFolderItem() {
        final FileObject object = new FileObject("", FOLDER_PLACEHOLDER, false, 0, FOLDER_PLACEHOLDER);
        object.addAttribute(CONTENTS_ATTRIBUTE, ".");
        return object;
    }

    /**
     * Registers an action that can be performed on a FileObject item
     * @param action    The action to be registered
     */
    default void registerAction(ItemAction action) { }

    /**
     * Returns the list of the actions registered with this storage service. If no action is registered, the result
     * should be <code>null</code>.
     */
    default List<ItemAction> getRegisteredActions() { return null; }

    /**
     * Executes the action with the given name on an item
     * @param actionName    The action name
     * @param item          The FileObject item onto which to execute the action
     */
    default void execute(String actionName, FileObject item) throws Exception { }

    /**
     * Computes the hash for the given file or folder (incl. its subfolders and files).
     * @param path  The path for which to compute the hash
     */
    default String computeHash(String path) throws IOException, NoSuchAlgorithmException {
        return Crypto.hash(new ArrayList<>() {{ add(path); }});
    }
}
