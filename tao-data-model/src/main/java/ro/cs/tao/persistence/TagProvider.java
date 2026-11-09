package ro.cs.tao.persistence;

import ro.cs.tao.Tag;
import ro.cs.tao.component.enums.TagType;

import java.util.List;

/**
 * Provides an interface for managing {@link Tag} entities uniquely identified by a {@link Long} identifier.
 * This interface extends {@link EntityProvider} to include tag-specific operations, such as listing
 * tags based on their corresponding {@link TagType}.
 *
 * The {@link TagProvider} is intended to abstract the persistence and retrieval details of tags,
 * enabling operations such as listing and retrieving tags while ensuring compliance with the
 * underlying domain model.
 *
 * @see Tag
 * @see TagType
 * @see EntityProvider
 */
public interface TagProvider extends EntityProvider<Tag, Long> {

    /**
     * Retrieves a list of tags associated with the specified type.
     *
     * @param type the type of the tags to be retrieved
     * @return a list of Tag objects that match the specified type
     */
    List<Tag> list(TagType type);
}
