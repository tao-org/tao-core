package ro.cs.tao;

import ro.cs.tao.component.LongIdentifiable;
import ro.cs.tao.component.enums.TagType;

/**
 * Represents a tag entity that is identified by a unique long ID and is characterized
 * by a specific {@link TagType} and an associated text.
 *
 * This class extends {@code LongIdentifiable}, allowing it to inherit the
 * functionality for managing a unique long identifier. Tags can be used to
 * categorize or label objects according to their {@link TagType} and textual
 * details.
 */
public class Tag extends LongIdentifiable {
    private TagType tagType;
    private String text;

    public Tag() { }

    public Tag(TagType tagType, String text) {
        this.tagType = tagType;
        this.text = text;
    }

    public TagType getTagType() { return tagType; }
    public void setTagType(TagType tagType) { this.tagType = tagType; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
