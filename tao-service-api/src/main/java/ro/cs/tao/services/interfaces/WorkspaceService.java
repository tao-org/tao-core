package ro.cs.tao.services.interfaces;

/**
 * Service for managing the user workspaces
 */
public interface WorkspaceService extends TAOService {

    void startWatching();

    void stopWatching();

    void registerUser(String userName);

    void unregisterUser(String userName);
}
