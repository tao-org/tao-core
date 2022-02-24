package ro.cs.tao.services.interfaces;

import ro.cs.tao.topology.Workspace;

import java.util.List;

public interface WorkspaceService extends CRUDService<Workspace, Integer> {

    List<Workspace> getByUser(String userName);
}
