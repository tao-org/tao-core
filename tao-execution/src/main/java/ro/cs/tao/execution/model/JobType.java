package ro.cs.tao.execution.model;

import jakarta.xml.bind.annotation.XmlEnum;
import ro.cs.tao.TaoEnum;

@XmlEnum(Integer.class)
public enum JobType implements TaoEnum<Integer> {

    EXECUTION(0, "Native execution", null),
    JSON_EXPORT(1, "Script as JSON", ".json"),
    CWL_EXPORT(2, "Script as CWL", ".cwl"),
    BASH_EXPORT(3, "Script as Bash script", ".sh"),
    ARGO_EXPORT(4, "Script as ARGO script", ".yaml"),
    PYTHON_EXPORT(5, "Script as Python script", ".py");

    private final int value;
    private final String description;
    private final String outputExtension;

    JobType(int value, String description, String extension) {
        this.value = value;
        this.description = description;
        this.outputExtension = extension;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }

    public String getOutputExtension() {
        return outputExtension;
    }
}

