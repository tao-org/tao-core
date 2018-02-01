package ro.cs.tao.workflow;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Workflow visibility enumeration
 * @author Cosmin Cara
 */
@XmlEnum(Integer.class)
public enum Visibility {
    /**
     * The workflow is public
     */
    @XmlEnumValue("1")
    PUBLIC(1),
    /**
     * The workflow is private (visible only to the author)
     */
    @XmlEnumValue("2")
    PRIVATE(2);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    Visibility(final int s)
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
        for (Visibility type : values())
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