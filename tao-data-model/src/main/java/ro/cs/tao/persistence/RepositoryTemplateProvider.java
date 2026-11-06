package ro.cs.tao.persistence;

import ro.cs.tao.workspaces.RepositoryTemplate;

/**
 * Defines an interface for managing {@link RepositoryTemplate} entities in the persistence layer.
 * This provider extends {@link EntityProvider} with {@code RepositoryTemplate} as the entity type
 * and {@code String} as the identifier type.
 *
 * Implementations of this interface are responsible for providing CRUD operations for
 * {@link RepositoryTemplate} entities such as retrieval, creation, updating, deletion, and
 * checking existence. They may also include additional logic specific to the requirements
 * of working with {@link RepositoryTemplate} entities.
 */
public interface RepositoryTemplateProvider extends EntityProvider<RepositoryTemplate, String> {

}
