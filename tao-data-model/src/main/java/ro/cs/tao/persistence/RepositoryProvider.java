package ro.cs.tao.persistence;

import ro.cs.tao.workspaces.Repository;

import java.util.List;

public interface RepositoryProvider extends EntityProvider<Repository, Integer> {

    List<Repository> getByUser(String userName);
}
