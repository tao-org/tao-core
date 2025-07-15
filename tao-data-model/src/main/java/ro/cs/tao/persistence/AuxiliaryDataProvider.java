package ro.cs.tao.persistence;

import ro.cs.tao.eodata.AuxiliaryData;

import java.util.List;

/**
 * Provides methods to manage and retrieve {@link AuxiliaryData} entities.
 * This interface allows interaction with auxiliary data through user and location-based queries.
 * It extends the generic {@link EntityProvider} interface for basic CRUD operations.
 */
public interface AuxiliaryDataProvider extends EntityProvider<AuxiliaryData, String> {

    /**
     * Retrieves a list of auxiliary data associated with a specific user.
     *
     * @param userName the name of the user whose auxiliary data is to be retrieved
     * @return a list of {@code AuxiliaryData} objects associated with the given user
     */
    List<AuxiliaryData> list(String userName);
    /**
     * Retrieves a list of auxiliary data associated with a specific user and filtered by the provided locations.
     *
     * @param userName the name of the user whose auxiliary data is to be retrieved
     * @param locations varargs specifying one or more locations to filter the auxiliary data
     * @return a list of {@code AuxiliaryData} objects associated with the given user and filtered by the specified locations
     */
    List<AuxiliaryData> list(String userName, String...locations);
    /**
     * Retrieves an {@code AuxiliaryData} object associated with a specific location.
     *
     * @param location the location used to query the corresponding auxiliary data
     * @return the {@code AuxiliaryData} object associated with the provided location,
     *         or {@code null} if no data is found for the given location
     */
    AuxiliaryData getByLocation(String location);
}
