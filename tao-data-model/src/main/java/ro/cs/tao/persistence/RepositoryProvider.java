package ro.cs.tao.persistence;

import ro.cs.tao.workspaces.Repository;

import java.util.List;

public interface RepositoryProvider extends EntityProvider<Repository, String> {

    List<Repository> getByUser(String userName);
    Repository getByUserAndName(String userName, String repoName);
}
