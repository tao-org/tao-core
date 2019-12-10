/**
 * 
 */
package ro.cs.tao.services.model.scheduling;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The scheduling mode.
 * 
 * @author Lucian Barbulescu
 *
 */
@XmlEnum(Integer.class)
public enum SchedulingMode implements TaoEnum<Integer> {
	/**
     * Normal execution. The execution will use the parameter values specified by the user.
     */
    @XmlEnumValue("1")
    NORMAL(1, "Normal"),
    /**
     * Incremental execution. The execution will use a different value for the start date field.
     * The new value is the date of the last product plus 1 minute.
     */
    @XmlEnumValue("2")
    INCREMENTAL(2, "Incremental");

    private final int value;
    private final String description;

    SchedulingMode(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
