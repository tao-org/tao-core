package ro.cs.tao.persistence.audit;

import jakarta.persistence.*;
import ro.cs.tao.component.Identifiable;
import ro.cs.tao.datasource.DataSourceCredentials;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.user.User;
import ro.cs.tao.utils.Crypto;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility class designed to handle encryption and decryption operations for entity objects
 * in a persistence context. It leverages JPA lifecycle event annotations to perform required
 * transformations on the entities' sensitive fields before they are persisted and after
 * they are loaded from the database.
 *
 * @param <T> The type of the entity for which encryption and decryption operations
 * are applied.
 */
public class EncryptDecryptListener<T extends Identifiable> {

    /**
     * Handles the lifecycle events of an entity after it is loaded, persisted, or updated
     * in the persistence context. This method ensures that specific actions are performed
     * on the entity as part of its post-load, post-persist, or post-update processing.
     *
     * The implementation details are delegated to the {@code onLoadImpl} method, which
     * must be overridden by subclasses to define the concrete behavior for handling
     * the given entity.
     *
     * @param entity the entity instance that was loaded, persisted, or updated. It must
     *               be of the generic type {@code T}.
     */
    @PostLoad
    @PostPersist
    @PostUpdate
    public void onLoad(T entity) {
        getHelper(entity).decryptFields(entity);
    }

    /**
     * Handles the lifecycle events before an entity is persisted or updated
     * in the persistence context. This method ensures that specific actions
     * are performed on the entity prior to saving it, such as applying transformations,
     * encryption, or setting metadata fields.
     *
     * Delegates implementation details to the {@code onBeforeSaveImpl} method,
     * which must be overridden by subclasses to provide concrete behavior.
     *
     * @param entity the entity instance that is about to be persisted or updated.
     *               It must be of the generic type {@code T}.
     */
    @PrePersist
    @PreUpdate
    public void onBeforeSave(T entity) {
        getHelper(entity).encryptFields(entity);
    }

    protected String decrypt(String value, String key) {
        return Crypto.decrypt(value, key);
    }

    protected String encrypt(String value, String key) {
        return Crypto.encrypt(value, key);
    }

    private EntityHelper<T> getHelper(T entity) {
        if (entity instanceof DataSourceCredentials) {
            return (EntityHelper<T>) new DataSourceCredentialsHelper();
        } else if (entity instanceof Query) {
            return (EntityHelper<T>) new QueryHelper();
        } else if (entity instanceof NodeDescription) {
            return (EntityHelper<T>) new NodeDescriptionHelper();
        } else if (entity instanceof User) {
            return (EntityHelper<T>) new UserHelper();
        } else {
            throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass().getName());
        }
    }

    /**
     * EntityHelper provides a generic interface for handling sensitive field encryption
     * and decryption within an entity of a specified type. This interface is intended
     * to be implemented by classes that define the concrete behavior for securing
     * sensitive data fields of their respective entities.
     *
     * @param <T> the type of the entity for which encryption and decryption logic is implemented
     */
    private interface EntityHelper<T> {
        void encryptFields(T entity);
        void decryptFields(T entity);
    }

    /**
     * DataSourceCredentialsHelper is a private class implementing the EntityHelper interface
     * for the DataSourceCredentials entity. It provides methods to securely handle sensitive
     * fields within the DataSourceCredentials entity by encrypting them before saving and
     * decrypting them when loading or accessing the entity. The class ensures that sensitive
     * information such as passwords and secrets are protected during persistence operations.
     *
     * The encryption and decryption of fields are performed using the encrypt and decrypt
     * methods, with the username serving as a key for these operations.
     *
     * This class is intended to be used internally within the enclosing EncryptDecryptListener
     * class to handle the secure management of sensitive data fields within a specific type
     * of entity.
     */
    private class DataSourceCredentialsHelper implements EntityHelper<DataSourceCredentials> {
        @Override
        public void decryptFields(DataSourceCredentials entity) {
            if (entity != null) {
                entity.setPassword(decrypt(entity.getPassword(), entity.getUserName()));
                entity.setSecret(decrypt(entity.getSecret(), entity.getUserName()));
            }
        }

        @Override
        public void encryptFields(DataSourceCredentials entity) {
            if (entity != null) {
                entity.setPassword(encrypt(entity.getPassword(), entity.getUserName()));
                entity.setSecret(encrypt(entity.getSecret(), entity.getUserName()));
            }
        }
    }

    /**
     * The QueryHelper class provides specific implementations for handling the
     * encryption and decryption of sensitive fields within a Query entity.
     * This class is an inner private implementation of the EntityHelper interface.
     *
     * It defines the logic for securing sensitive data fields, such as passwords,
     * by applying encryption before persisting the entity and decryption after the entity
     * is retrieved or loaded.
     *
     * Methods in this class:
     *
     * 1. encryptFields(Query entity):
     *    This method applies encryption to the sensitive fields of the given Query
     *    entity. It ensures that only secured data is persisted to storage.
     *
     * 2. decryptFields(Query entity):
     *    This method reverses the encryption applied to the sensitive fields of the
     *    given Query entity. It restores the original plaintext values after the entity
     *    is retrieved or loaded from storage.
     *
     * Both methods rely on utility functions for cryptographic operations defined in
     * the containing class.
     */
    private class QueryHelper implements EntityHelper<Query> {

        @Override
        public void encryptFields(Query entity) {
            if (entity != null) {
                entity.setPassword(encrypt(entity.getPassword(), entity.getUser()));
            }
        }

        @Override
        public void decryptFields(Query entity) {
            if (entity != null) {
                entity.setPassword(decrypt(entity.getPassword(), entity.getUser()));
            }
        }
    }

    /**
     * NodeDescriptionHelper is a private helper class implementing the EntityHelper interface
     * to handle encryption and decryption of sensitive fields in the NodeDescription entity.
     * It ensures that fields such as user credentials are securely encrypted before being saved
     * and decrypted after being loaded or accessed in the persistence context.
     *
     * This helper focuses on securing the `userPass` field of the NodeDescription entity
     * by applying encryption and decryption based on the corresponding `userName`.
     * The encryption and decryption processes are delegated to methods provided
     * by the containing class, leveraging predefined cryptographic mechanisms.
     *
     * The primary use cases for this class include:
     * 1. Encrypting sensitive data (e.g., passwords) before persisting the entity.
     * 2. Decrypting sensitive data after the entity is loaded or accessed.
     *
     * This class is designed as a utility for the EncryptDecryptListener framework,
     * ensuring secure handling of sensitive properties specific to NodeDescription entities.
     */
    private class NodeDescriptionHelper implements EntityHelper<NodeDescription> {
        @Override
        public void decryptFields(NodeDescription entity) {
            if (entity != null) {
                entity.setUserPass(decrypt(entity.getUserPass(), entity.getUserName()));
            }
        }

        @Override
        public void encryptFields(NodeDescription entity) {
            if (entity != null) {
                entity.setUserPass(encrypt(entity.getUserPass(), entity.getUserName()));
            }
        }
    }

    /**
     * UserHelper is a private implementation of the EntityHelper interface
     * that handles encryption and decryption of sensitive fields for the User entity.
     * It ensures that sensitive data, such as passwords, is securely managed during
     * the lifecycle of the User entity.
     */
    private class UserHelper implements EntityHelper<User> {

        @Override
        public void encryptFields(User entity) {
            if (entity != null) {
                final LocalDateTime now = LocalDateTime.now();
                final boolean isDecrypted = entity.getPassword() != null && decrypt(entity.getPassword(), entity.getUsername()).equals(entity.getPassword());
                String encryptedPassword = encrypt(entity.getPassword(), entity.getUsername());
                if (entity.getModified() == null || Duration.between(entity.getModified(), now).getSeconds() > 3 || isDecrypted) {
                    entity.setPassword(encryptedPassword);
                    if (entity.getCreated() == null) {
                        entity.setCreated(now);
                    }
                    entity.setModified(now);
                }
            }
        }

        @Override
        public void decryptFields(User entity) {
            if (entity != null) {
                entity.setPassword(decrypt(entity.getPassword(), entity.getUsername()));
            }
        }
    }
}

