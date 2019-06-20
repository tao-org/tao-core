package ro.cs.tao.component;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A parameter expansion rule specifies how an array parameter should expand into command line.
 *
 * @author Cosmin Cara
 * @since 1.0
 */
@XmlRootElement(name = "expansionRule")
public class ParameterExpansionRule {
    private String parameterId;
    private boolean joinValues;
    private String separator;

    public String getParameterId() {
        return parameterId;
    }

    public void setParameterId(String parameterId) {
        this.parameterId = parameterId;
    }

    /**
     * Getter for joinValues.
     * If <code>true</code>, then the values will be joined using the separator. For example: -param v1 v2 v3.
     * If <code>false</code>, then the values will be individually expanded using the parameter identifier.
     * For example: -param v1 -param v2 -param v3
     */
    public boolean isJoinValues() { return joinValues; }

    /**
     * Setter for joinValues.
     * @param joinValues    Flag indicating the expansion behavior ({@see ParameterExpansionRule.isJoinValues()}
     */
    public void setJoinValues(boolean joinValues) { this.joinValues = joinValues; }

    /**
     * Returns the separator of the array elements
     */
    public String getSeparator() { return separator; }
    /**
     * Sets the separator of the array elements
     */
    public void setSeparator(String separator) { this.separator = separator; }
}
