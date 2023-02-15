package ro.cs.tao.services.model;

import ro.cs.tao.persistence.EOProductProvider;
import ro.cs.tao.services.interfaces.StorageService;
import ro.cs.tao.utils.executors.monitoring.ProgressListener;
import ro.cs.tao.workspaces.RepositoryType;

import java.nio.file.Path;

/**
 * Interface representing an action that can be performed on an item from a repository
 *
 * @author  Cosmin Cara
 * @since   1.4.1
 */
public interface ItemAction {
    /**
     * The name of the action.
     */
    String name();
    /**
     * Checks if this action can be registered with the given storage service
     * @param repositoryType    The type of repository for which this action is intended
     */
    boolean isIntendedFor(RepositoryType repositoryType);

    /**
     * Returns an array of filters of supported files (i.e., [".tif", ".nc"]).
     * The default implementation returns <code>null</code>, which means all kind of files/folders
     */
    default String[] supportedFiles() { return null; }
    /**
     * Execution of this action on the given item
     * @param item  The item onto which to execute the action
     * @throws Exception  If something goes wrong
     */
    default Path doAction(final Path item) throws Exception { return item; }
    /**
     * Execution of this action on the given item
     * @param item  The item onto which to execute the action
     * @param destination The destination of the action result
     * @throws Exception  If something goes wrong
     */
    default Path doAction(final Path item, final Path destination) throws Exception { return null; }

    default void setProductProvider(EOProductProvider productProvider) { }

    default void setActionUser(String user) { }

    default void associateWith(StorageService storageService) { }

    default void setProgressListener(ProgressListener listener) { }
}
