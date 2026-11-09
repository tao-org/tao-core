package ro.cs.tao.persistence;

import ro.cs.tao.workspaces.Site;

import java.util.List;

/**
 * Represents a provider for managing {@link Site} entities. This interface
 * extends the {@link EntityProvider} to handle sites uniquely identified
 * by a {@link String} identifier.
 *
 * In addition to the common entity operations defined in {@link EntityProvider},
 * the interface provides a method for retrieving a list of sites associated
 * with a specific user.
 *
 * @see EntityProvider
 * @see Site
 */
public interface SiteProvider extends EntityProvider<Site, String> {

    /**
     * Retrieves a list of sites associated with the specified user.
     *
     * @param userName the username of the user whose associated sites are to be retrieved
     * @return a list of Site objects corresponding to the specified user
     */
    List<Site> getByUser(String userName);
}