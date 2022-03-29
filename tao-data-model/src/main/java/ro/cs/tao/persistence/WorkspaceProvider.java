package ro.cs.tao.persistence;

import ro.cs.tao.workspaces.Workspace;

import java.util.List;

public interface WorkspaceProvider extends EntityProvider<Workspace, Integer> {

    List<Workspace> getByUser(String userName);
}
