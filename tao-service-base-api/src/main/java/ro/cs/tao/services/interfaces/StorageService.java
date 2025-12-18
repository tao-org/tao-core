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
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.utils.executors.monitoring.ProgressListener;
import ro.cs.tao.workspaces.Repository;
import ro.cs.tao.workspaces.RepositoryType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipOutputStream;

/**
 * Service interface for manipulation of files and folders (server-side).
 *
 * @author Cosmin Cara
 */
public interface StorageService<T> extends TAOService {

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
     * Determines whether a given repository type matches the intended purpose.
     *
     * @param repositoryType the type of the repository to evaluate
     * @return true if the repository type is intended for the specified purpose, false otherwise
     */
    boolean isIntendedFor(RepositoryType repositoryType);

    /**
     * Associates a repository configuration to this service instance
     * @param repository    The repository
     */
    default void associate(Repository repository) { }

    /**
     * Sets the product provider to be used.
     *
     * @param productProvider the instance of EOProductProvider to set as the product provider
     */
    default void setProductProvider(EOProductProvider productProvider) { }

    /**
     * Sets the vector data provider responsible for supplying vector data.
     * This method allows integration with a custom data provider that
     * handles the retrieval and management of vector data for this component.
     *
     * @param vectorDataProvider the VectorDataProvider instance to be used
     *                           for managing vector data
     */
    default void setVectorDataProvider(VectorDataProvider vectorDataProvider) { }

    /**
     * Sets the auxiliary data provider to supply additional data required by this component.
     *
     * @param auxiliaryDataProvider the provider responsible for supplying auxiliary data
     */
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
     * @param relativeFolder    The folder path, relative to the workspace of the calling user
     * @param description       A description for the file
     * @throws  Exception if the file cannot be stored
     */
    default String storeFile(InputStream stream, String relativeFolder, String description) throws Exception {
        return storeFile(stream, 0, relativeFolder, description);
    }

    /**
     * Stores a file from the given input stream to the specified relative folder with an associated description.
     *
     * @param stream         the input stream containing the file data to be stored
     * @param length         the length of the file in bytes
     * @param relativeFolder the relative folder path where the file should be stored
     * @param description    a description associated with the file being stored
     * @throws Exception     if an error occurs during file storage
     */
    default String storeFile(InputStream stream, long length, String relativeFolder, String description) throws Exception {
        return null;
    }

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

    /**
     * Renames a file or directory from the specified source path to the target path.
     *
     * @param source the current name or path of the file or directory to be renamed
     * @param target the new desired name or path for the file or directory
     * @throws IOException if an I/O error occurs during the renaming process
     */
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

    /**
     * Lists files from the specified path based on the provided parameters.
     *
     * @param fromPath       the starting directory path from which files will be listed
     * @param exclusions     a set of file or directory names to be excluded from the results
     * @param lastItem       the last processed item, used for pagination or continuation of listing
     * @param depth          the maximum depth of directories to traverse
     * @param excludedPaths  a set of specific paths to be excluded from traversal
     * @return a list of FileObject representing the files found based on the given criteria
     * @throws IOException if an I/O error occurs while accessing the file system
     */
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
     * Retrieves a list of file objects based on the selected path, starting at the storage root.
     * If the selected path is empty or null, an empty list will be returned.
     * If the root path is selected, it retrieves the root folder and its children.
     * Otherwise, it navigates the storage tree, marking the relevant nodes as selected
     * and returns the sub-tree starting at the specified path.
     *
     * @param selectedPath The path to navigate within the storage for retrieving files.
     *                     This path must be relative to the root of the storage.
     * @return A list of {@code FileObject} representing the file structure for the given selection path.
     *         If the specified path is invalid or empty, the list will be empty.
     * @throws IOException If any errors occur while retrieving the file structure.
     */
    default List<FileObject> getForSelection(String selectedPath) throws IOException {
        if (StringUtilities.isNullOrEmpty(selectedPath)) {
            return List.of();
        }
        if (selectedPath.equals("/")) {
            return listFiles("/", null, null, 1);
        } else {
            String[] tokens = (selectedPath.startsWith("/") ? selectedPath.substring(1) : selectedPath).split("/");
            final List<FileObject> result = new ArrayList<>();
            result.addAll(listFiles("/", null, null, 1));
            FileObject parent = result.stream().filter(f -> f.getRelativePath().equals(tokens[0])).findFirst().get();
            parent.setSelected(true);
            final StringBuilder path = new StringBuilder();
            path.append(tokens[0]);
            for (int i = 1; i < tokens.length; i++) {
                List<FileObject> children = listFiles(parent.getRelativePath(), null, null, 1);
                if (i < tokens.length - 1) {
                    children.removeIf(f -> !f.isFolder());
                }
                parent.setChildren(children);
                path.append("/").append(tokens[i]);
                parent.setSelected(children.isEmpty());
                parent = children.stream().filter(f -> f.getRelativePath().equals(path.toString())).findFirst().get();
                parent.setSelected(true);
            }
            return result;
        }
    }

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
    InputStream download(String path) throws IOException;

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
    default <S extends InputStream> String readAsText(S stream, int lines, int skipLines) throws IOException {
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

    /**
     * Transfers a file from a remote storage service to the current storage service.
     *
     * @param other      the remote storage service from which the file will be downloaded
     * @param thisPath   the local path in the current storage service where the file will be stored
     * @param remotePath the path in the remote storage service from which the file will be downloaded
     * @throws Exception if an error occurs during the file transfer process
     */
    default void transferFrom(StorageService other, String thisPath, String remotePath) throws Exception {
        storeFile(other.download(remotePath), thisPath, remotePath);
    }

    /**
     * Retrieves a map representing user storage, where the keys are user identifiers
     * and the values are associated storage amounts in bytes.
     *
     * @return a map containing user identifiers as keys and their corresponding storage amounts as values in bytes.
     */
    default Map<String, Long> getUserStorage() {
        return new HashMap<>();
    }
}
