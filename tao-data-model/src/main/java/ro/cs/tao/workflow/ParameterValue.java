package ro.cs.tao.workflow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "parameter")
public class ParameterValue implements Serializable {
    private String parameterName;
    private String parameterValue;

    @XmlElement(name = "name")
    public String getParameterName() { return parameterName; }
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }

    @XmlElement(name = "value")
    public String getParameterValue() { return parameterValue; }
    public void setParameterValue(String parameterValue) { this.parameterValue = parameterValue; }
}
