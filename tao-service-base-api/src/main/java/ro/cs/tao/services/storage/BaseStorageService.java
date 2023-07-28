package ro.cs.tao.services.storage;

import ro.cs.tao.ListenableInputStream;
import ro.cs.tao.services.interfaces.StorageService;
import ro.cs.tao.services.model.FileObject;
import ro.cs.tao.services.model.ItemAction;
import ro.cs.tao.utils.executors.monitoring.ProgressListener;
import ro.cs.tao.workspaces.Repository;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all storage service implementations.
 * It defines method that are common to all implementors.
 *
 * @param <T>   The type of objects that can be used to upload items to this storage
 * @param <U>   The type of objects that can be used to download items from this storage
 */
public abstract class BaseStorageService<T, U> implements StorageService<T, U> {
    protected ProgressListener progressListener;
    private Repository repository;
    private Map<String, ItemAction> actions;

    @Override
    public void associate(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    @Override
    public void registerAction(ItemAction action) {
        if (this.actions == null) {
            this.actions = new LinkedHashMap<>();
        }
        if (!this.actions.containsKey(action.name())) {
            action.associateWith(this);
            this.actions.put(action.name(), action);
        }
    }

    @Override
    public List<ItemAction> getRegisteredActions() {
        return this.actions != null ? new ArrayList<>(this.actions.values()) : null;
    }

    @Override
    public void execute(String actionName, FileObject item) throws Exception {
        final ItemAction action;
        if (this.actions == null || (action = this.actions.get(actionName)) == null) {
            throw new IllegalArgumentException("No such action");
        }
        final String path = repository.resolve(item.getRelativePath());
        action.setActionUser(repository.getUserId());
        action.doAction(Paths.get(path));
    }

    protected InputStream wrapStream(InputStream inputStream) {
        return this.progressListener != null && !(inputStream instanceof ListenableInputStream)
               ? new ListenableInputStream(inputStream, this.progressListener)
               : inputStream;
    }

    protected Repository repository() {
        if (this.repository == null) {
            throw new RuntimeException("Service has no associated repository");
        }
        return this.repository;
    }
}
