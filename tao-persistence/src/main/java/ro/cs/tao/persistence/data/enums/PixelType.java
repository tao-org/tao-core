package ro.cs.tao.persistence.data.enums;

/**
 * Created by oana on 7/13/2017.
 */
public enum PixelType {
    UINT8(1),
    INT8(2),
    UINT16(3),
    INT16(4),
    UINT32(5),
    INT32(6),
    FLOAT32(7),
    FLOAT64(8);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    PixelType(final int s)
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
        for (PixelType type : values())
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
