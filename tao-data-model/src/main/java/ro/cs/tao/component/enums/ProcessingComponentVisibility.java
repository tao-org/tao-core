package ro.cs.tao.component.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Oana H.
 */
@XmlEnum(Integer.class)
public enum ProcessingComponentVisibility {
    /**
     * TAO system build-in component
     */
    @XmlEnumValue("1")
    SYSTEM(1),
    /**
     * User defined component
     */
    @XmlEnumValue("2")
    USER(2),
    /**
     * Contributor defined component
     */
    @XmlEnumValue("3")
    CONTRIBUTOR(3);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    ProcessingComponentVisibility(final int s)
    {
        value = s;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }

    /**
     * Retrieve string enum token corresponding to the integer identifier
     * @param value the integer value identifier
     * @return the string token corresponding to the integer identifier
     */
    public static String getEnumConstantNameByValue(final int value)
    {
        for (ProcessingComponentVisibility type : values())
        {
            if ((String.valueOf(value)).equals(type.toString()))
            {
                // return the name of the enum constant having the given value
                return type.name();
            }
        }
        return null;
    }
}
