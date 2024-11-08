package ro.cs.tao.services.interfaces;

/**
 * Service for managing the user workspaces
 */
public interface RepositoryWatcherService extends TAOService {

    void startWatching();

    void stopWatching();

    void registerUser(String userId);

    void unregisterUser(String userId);
}
