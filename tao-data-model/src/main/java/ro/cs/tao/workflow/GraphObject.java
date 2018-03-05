package ro.cs.tao.workflow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class GraphObject {

    protected String name;
    protected LocalDateTime created;
    private List<ParameterValue> customValues;

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "created")
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    @XmlElementWrapper(name = "customValues")
    public List<ParameterValue> getCustomValues() { return customValues; }
    public void setCustomValues(List<ParameterValue> customValues) { this.customValues = customValues; }
    public void addCustomValue(String name, String value) {
        ParameterValue parameterValue = new ParameterValue();
        parameterValue.setParameterName(name);
        parameterValue.setParameterValue(value);
        if (this.customValues == null) {
            this.customValues = new ArrayList<>();
        }
        this.customValues.add(parameterValue);
    }
}
