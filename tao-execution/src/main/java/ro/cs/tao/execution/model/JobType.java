package ro.cs.tao.execution.model;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum(Integer.class)
public enum JobType implements TaoEnum<Integer> {

    EXECUTION(0, "Native execution"),
    JSON_EXPORT(1, "Script as JSON"),
    CWL_EXPORT(2, "Script as CWL"),
    BASH_EXPORT(3, "Script as Bash script");

    private final int value;
    private final String description;

    JobType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }


}
