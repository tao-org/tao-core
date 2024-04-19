package ro.cs.tao.services.interfaces;

import ro.cs.tao.eodata.naming.NamingRule;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.services.model.component.NamingRuleTokens;

import java.util.List;
import java.util.Map;

/**
 * Service for managing operations for name tokens.
 *
 * @author Cosmin Cara
 */
public interface NameTokenService extends CRUDService<NamingRule, Integer> {
    /**
     * Retrieves the name tokens for a given sensor, if defined.
     * @param sensor    The sensor (satellite)
     */
    Map<String, String> getNameTokens(String sensor);

    /**
     * Retrieves any name token rules that can be used for the given workflow.
     * @param workflowNodeId    The workflow identifier
     * @throws PersistenceException
     */
    List<NamingRuleTokens> findTokens(long workflowNodeId) throws PersistenceException;
}
