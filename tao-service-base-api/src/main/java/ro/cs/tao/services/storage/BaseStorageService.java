package ro.cs.tao.services.storage;

import ro.cs.tao.ListenableInputStream;
import ro.cs.tao.ProgressListener;
import ro.cs.tao.services.interfaces.StorageService;
import ro.cs.tao.workspaces.Repository;

import java.io.InputStream;

public abstract class BaseStorageService<T, U> implements StorageService<T, U> {
    protected ProgressListener progressListener;
    private Repository repository;

    @Override
    public void associate(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    protected InputStream wrapStream(InputStream inputStream) {
        return this.progressListener != null ? new ListenableInputStream(inputStream, this.progressListener) : inputStream;
    }

    protected Repository repository() {
        if (this.repository == null) {
            throw new RuntimeException("Service has no associated repository");
        }
        return this.repository;
    }
}
