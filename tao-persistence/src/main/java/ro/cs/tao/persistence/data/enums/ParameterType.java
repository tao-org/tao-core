package ro.cs.tao.persistence.data.enums;

/**
 * Created by oana on 8/1/2017.
 */
public enum ParameterType {

    /**
     * Regular parameter
     */
    REGULAR_PARAMETER(1),
    /**
     * Template parameter
     */
    TEMPLATE_PARAMETER(2);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    ParameterType(final int s)
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
        for (ParameterType type : values())
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
