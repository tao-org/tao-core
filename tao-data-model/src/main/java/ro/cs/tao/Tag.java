package ro.cs.tao;

import ro.cs.tao.component.LongIdentifiable;
import ro.cs.tao.component.enums.TagType;

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
