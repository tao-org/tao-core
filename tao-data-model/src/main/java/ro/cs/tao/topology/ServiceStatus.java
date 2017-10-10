package ro.cs.tao.topology;

import javax.xml.bind.annotation.XmlEnum;

/**
 * Possible statuses of a service.
 *
 * @author Cosmin Cara
 */
@XmlEnum
public enum ServiceStatus {
    NOT_FOUND(1),
    INSTALLED(2),
    UNINSTALLED(3),
    ERROR(4);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    ServiceStatus(final int s)
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
        for (ServiceStatus type : values())
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
