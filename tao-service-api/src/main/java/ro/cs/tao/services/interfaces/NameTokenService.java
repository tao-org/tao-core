package ro.cs.tao.services.interfaces;

import ro.cs.tao.eodata.naming.NamingRule;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.services.model.component.NamingRuleTokens;

import java.util.List;
import java.util.Map;

public interface NameTokenService extends CRUDService<NamingRule, Integer> {

    Map<String, String> getNameTokens(String sensor);

    List<NamingRuleTokens> findTokens(long workflowNodeId) throws PersistenceException;
}
