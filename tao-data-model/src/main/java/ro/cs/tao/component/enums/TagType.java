package ro.cs.tao.component.enums;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum TagType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    TOPOLOGY_NODE(1, "Topology node tag"),
    @XmlEnumValue("2")
    COMPONENT(2, "Processing component tag"),
    @XmlEnumValue("3")
    DATASOURCE(3, "DataSource tag"),
    @XmlEnumValue("4")
    WORKFLOW(4, "Workflow tag");

    private final int value;
    private final String description;

    TagType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }

}
