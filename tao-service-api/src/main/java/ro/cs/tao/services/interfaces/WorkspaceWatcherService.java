package ro.cs.tao.services.interfaces;

/**
 * Service for managing the user workspaces
 */
public interface WorkspaceWatcherService extends TAOService {

    void startWatching();

    void stopWatching();

    void registerUser(String userName);

    void unregisterUser(String userName);
}
