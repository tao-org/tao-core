package ro.cs.tao.persistence;

import ro.cs.tao.Tag;
import ro.cs.tao.component.enums.TagType;

import java.util.List;

public interface TagProvider extends EntityProvider<Tag, Long> {

    List<Tag> list(TagType type);
}
