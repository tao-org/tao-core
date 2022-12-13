package ro.cs.tao.services.model;

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
     * Execution of this action on the given item
     * @param item  The item onto which to execute the action
     * @throws Exception  If something goes wrong
     */
    default void doAction(final Path item) throws Exception { }
    /**
     * Execution of this action on the given item
     * @param item  The item onto which to execute the action
     * @param destination The destination of the action result
     * @throws Exception  If something goes wrong
     */
    default void doAction(final Path item, final Path destination) throws Exception { }
}
