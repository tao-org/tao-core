package ro.cs.tao.services.interfaces;

import ro.cs.tao.workspaces.Repository;

import java.util.List;

public interface RepositoryService extends CRUDService<Repository, Integer> {

    List<Repository> getByUser(String userName);
}
