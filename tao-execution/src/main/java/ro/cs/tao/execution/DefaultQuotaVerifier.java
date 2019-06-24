package ro.cs.tao.execution;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.user.User;

import java.security.Principal;

public class DefaultQuotaVerifier implements QuotaVerifier {

    private PersistenceManager persistenceManager;

    @Override
    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public boolean checkUserInputQuota(Principal principal) throws PersistenceException {
        if (principal == null) {
            throw new IllegalArgumentException("[principal] null");
        }
        String userName = principal.getName();
        User user = getPersistenceManager().findUserByUsername(userName);
        if (user == null) {
            throw new PersistenceException(String.format("User '%s' not found", userName));
        }
        final long targetQuota = user.getInputQuota();
        final long actualQuota = persistenceManager.getUserProductsSize(userName); //user.getActualInputQuota();
        return targetQuota == -1 || targetQuota > actualQuota;
    }

    @Override
    public boolean checkUserProcessingQuota(Principal principal) throws PersistenceException {
        if (principal == null) {
            throw new IllegalArgumentException("[principal] null");
        }
        User user = getPersistenceManager().findUserByUsername(principal.getName());
        if (user == null) {
            throw new PersistenceException(String.format("User '%s' not found", principal.getName()));
        }
        final long targetQuota = user.getProcessingQuota();
        final long actualQuota = user.getActualProcessingQuota();
        return targetQuota == -1 || targetQuota > actualQuota;
    }

    @Override
    public boolean checkUserProcessingResources(Principal principal, ExecutionJob job) throws PersistenceException {
        return false;
    }

    private PersistenceManager getPersistenceManager() throws PersistenceException {
        if (this.persistenceManager == null) {
            throw new PersistenceException("PersistenceManager not set");
        }
        return this.persistenceManager;
    }
}
