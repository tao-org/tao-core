package ro.cs.tao.persistence;

import ro.cs.tao.workspaces.Repository;

import java.util.List;

public interface RepositoryProvider extends EntityProvider<Repository, String> {

    List<Repository> getByUser(String userId);
    Repository getByUserAndName(String userId, String repoName);
    List<Repository> getUserSystemRepositories(String userId);
}
