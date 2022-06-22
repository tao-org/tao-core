package ro.cs.tao.services.interfaces;

import ro.cs.tao.workspaces.Repository;

import java.util.List;

public interface RepositoryService extends CRUDService<Repository, String> {

    List<Repository> getByUser(String userName);
    Repository getByUserAndName(String userName, String repositoryName);
}
