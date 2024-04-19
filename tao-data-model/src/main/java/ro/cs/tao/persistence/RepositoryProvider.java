package ro.cs.tao.persistence;

import ro.cs.tao.workspaces.Repository;

import java.util.List;

public interface RepositoryProvider extends EntityProvider<Repository, String> {

    /**
     * Retrieves all the repositories for a given user
     * @param userId    The user identifier
     */
    List<Repository> getByUser(String userId);
    /**
     * Retrieves a repository for a given user and repository name
     * @param userId    The user identifier
     * @param repoName  The name of the repository
     */
    Repository getByUserAndName(String userId, String repoName);
    /**
     * Retrieves the system repositories for a given user.
     * System repositories are created and assigned to a user at registration time.
     * @param userId    The user identifier
     */
    List<Repository> getUserSystemRepositories(String userId);
    /**
     * Retrieves the repository that is marked as destination for processing outputs for a given user.
     * @param userId    The user identifier
     */
    Repository getUserPersistentRepository(String userId);
    /**
     * Marks a repository as destination for processing outputs for a given user.
     * All the others repositories of the user will be unmarked.
     * @param userId    The user identifier
     * @param repositoryId  The repository identifier
     */
    void setUserPersistentRepository(String userId, String repositoryId) throws PersistenceException;
}
