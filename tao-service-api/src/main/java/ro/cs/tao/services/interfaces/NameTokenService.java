package ro.cs.tao.services.interfaces;

import ro.cs.tao.eodata.naming.NamingRule;

import java.util.Map;

public interface NameTokenService extends CRUDService<NamingRule, Integer> {

    Map<String, String> getNameTokens(String sensor);

}
