package ro.cs.tao.services.interfaces;

import ro.cs.tao.workspaces.Repository;

import java.util.List;

/**
 * Service interface for managing repositories in the system.
 * Extends the {@code CRUDService} interface, inheriting basic CRUD operations.
 */
public interface RepositoryService extends CRUDService<Repository, String> {

    /**
     * Retrieves a list of repositories associated with the specified user.
     *
     * @param userId the unique identifier of the user whose repositories are to be retrieved
     * @return a list of repositories belonging to the specified user
     */
    List<Repository> getByUser(String userId);

    /**
     * Retrieves a repository associated with a specific user and repository name.
     *
     * @param userId the unique identifier of the user to whom the repository belongs
     * @param repositoryName the name of the repository to retrieve
     * @return the repository that matches the specified user and repository name or null if no match is found
     */
    Repository getByUserAndName(String userId, String repositoryName);

    /**
     * Generates a URL for downloading an item (file or folder) from a repository.
     *
     * @param userId the unique identifier of the user requesting the download
     * @param repositoryId the unique identifier of the repository containing the item
     * @param item the path or identifier of the item to be downloaded
     * @param isFile flag indicating whether the item is a file (true) or folder (false)
     * @param base64Encoded flag indicating whether the output should be Base64 encoded or not
     * @return the URL string that can be used to download the specified item
     */
    String createDownloadURL(String userId, String repositoryId, String item, boolean isFile, boolean base64Encoded);
}
