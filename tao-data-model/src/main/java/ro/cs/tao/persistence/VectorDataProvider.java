package ro.cs.tao.persistence;

import ro.cs.tao.eodata.VectorData;

import java.util.List;

/**
 * Provides an interface for managing {@link VectorData} entities uniquely identified by a
 * {@link String} identifier. This interface extends {@link EntityProvider}, enabling standard
 * CRUD operations and additional functionality specific to vector-based data management.
 *
 * The {@code VectorDataProvider} interface defines methods to retrieve vector data entries
 * based on specific location criteria.
 *
 * @see VectorData
 * @see EntityProvider
 */
public interface VectorDataProvider extends EntityProvider<VectorData, String> {

    /**
     * Retrieves a list of {@link VectorData} objects associated with the specified location(s).
     *
     * @param locations one or more location identifiers used to filter the {@link VectorData} objects
     * @return a list of {@link VectorData} objects that match the given location criteria
     */
    List<VectorData> getByLocation(String...locations);

}
