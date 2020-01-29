package ro.cs.tao.persistence;

import ro.cs.tao.eodata.naming.NamingRule;

import java.util.List;

public interface NamingRuleProvider extends EntityProvider<NamingRule, Integer> {

    List<NamingRule> listBySensor(String sensor);

}
