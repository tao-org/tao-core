package ro.cs.tao.persistence.data.enums;
//
///**
// * Created by oana on 7/14/2017.
// */
//
//public enum DataSourceType {
//
//    /**
//     * Local Data Source
//     */
//    LOCAL_DATA_SOURCE(1),
//    /**
//     * SciHub Sentinel-1 Data Source
//     */
//    SCIHUB_SENTINEL_1_DATA_SOURCE(2),
//    /**
//     * SciHub Sentinel-2 Data Source
//     */
//    SCIHUB_SENTINEL_2_DATA_SOURCE(3),
//    /**
//     * AWS Sentinel-2 Data Source
//     */
//    AWS_SENTINEL_2_DATA_SOURCE(4),
//    /**
//     * AWS Landsat 8 Data Source
//     */
//    AWS_LANDSAT_8_DATA_SOURCE(5),
//    /**
//     * PEPS Sentinel-1 Data Source
//     */
//    PEPS_SENTINEL_1_DATA_SOURCE(6),
//    /**
//     * PEPS Sentinel-2 Data Source
//     */
//    PEPS_SENTINEL_2_DATA_SOURCE(7),
//    /**
//     * PEPS Sentinel-3 Data Source
//     */
//    PEPS_SENTINEL_3_DATA_SOURCE(8);
//
//    /**
//     * Numerical value for enum constants
//     */
//    private final int value;
//
//    /**
//     * Constructor
//     * @param s - the integer value identifier
//     */
//    DataSourceType(final int s)
//    {
//        value = s;
//    }
//
//    @Override
//    public String toString()
//    {
//        return String.valueOf(this.value);
//    }
//
//    /**
//     * Retrieve string enum token corresponding to the integer identifier
//     * @param value the integer value identifier
//     * @return the string token corresponding to the integer identifier
//     */
//    public static String getEnumConstantNameByValue(final int value)
//    {
//        for (DataSourceType type : values())
//        {
//            if ((String.valueOf(value)).equals(type.toString()))
//            {
//                // return the name of the enum constant having the given value
//                return type.name();
//            }
//        }
//        return null;
//    }
//}
