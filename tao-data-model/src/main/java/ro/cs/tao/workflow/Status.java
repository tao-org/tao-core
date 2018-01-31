package ro.cs.tao.workflow;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Workflow (editing) status enumeration
 * @author Cosmin Cara
 */
@XmlEnum(Integer.class)
public enum Status {
    /**
     * The workflow is draft (still in editing mode)
     */
    @XmlEnumValue("1")
    DRAFT(1),
    /**
     * The workflow is validated and ready to be executed, and may still be edited
     */
    @XmlEnumValue("2")
    READY(2),
    /**
     * The workflow was published and hence cannot be edited
     */
    @XmlEnumValue("2")
    PUBLISHED(3);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    Status(final int s)
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
        for (Status type : values())
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
