package ro.cs.tao.services.interfaces;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Service for managing the user workspaces
 */
public interface WorkspaceService extends TAOService {

    void walkAndRegisterDirectories(Path path) throws IOException;

    void watch();
}
