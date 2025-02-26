package ro.cs.tao.persistence;

import ro.cs.tao.eodata.naming.NamingRule;

import java.util.List;
import java.util.Set;

public interface NamingRuleProvider extends EntityProvider<NamingRule, Integer> {

    List<NamingRule> listBySensor(String sensor);
    Set<String> getSynonims(String sensor);
}
