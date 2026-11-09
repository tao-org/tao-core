package ro.cs.tao.persistence;

import ro.cs.tao.component.WebServiceAuthentication;

/**
 * Provides an abstraction layer for managing {@link WebServiceAuthentication} entities
 * uniquely identified by a {@link String} identifier.
 *
 * This interface extends {@link EntityProvider} to include generic CRUD operations
 * specific to {@link WebServiceAuthentication}, enabling persistence, retrieval,
 * and management of web service authentication details.
 *
 * Implementing classes can define additional behavior or override default operations
 * based on specific requirements for handling web service authentication entities.
 *
 * @see WebServiceAuthentication
 * @see EntityProvider
 */
public interface WebServiceAuthenticationProvider extends EntityProvider<WebServiceAuthentication, String> {
}
