package ro.cs.tao;

import ro.cs.tao.component.enums.TagType;

public class Tag {
    private long id;
    private TagType tagType;
    private String text;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public TagType getTagType() { return tagType; }
    public void setTagType(TagType tagType) { this.tagType = tagType; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
