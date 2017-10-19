package ro.cs.tao.eodata.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Image data formats
 */
@XmlEnum(Integer.class)
public enum DataFormat {
    /**
     * RASTER image data format
     */
    @XmlEnumValue("1")
    RASTER(1),
    /**
     * VECTOR image data format
     */
    @XmlEnumValue("2")
    VECTOR(2),
    /**
     * Other image data format
     */
    @XmlEnumValue("3")
    OTHER(3);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    DataFormat(final int s)
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
        for (DataFormat type : values())
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
