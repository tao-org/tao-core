package ro.cs.tao.persistence;

import ro.cs.tao.eodata.naming.NamingRule;

import java.util.List;
import java.util.Set;

/**
 * Provides methods to manage and retrieve NamingRule entities, which define associations
 * between satellite (sensor) product names and regular expressions used to extract tokens
 * from product names. This interface extends {@link EntityProvider}, offering additional
 * functionality specific to NamingRule entities.
 *
 * Methods in this interface include operations for retrieving naming rules by sensor
 * and fetching synonyms for a given sensor.
 */
public interface NamingRuleProvider extends EntityProvider<NamingRule, Integer> {

    /**
     * Retrieves a list of {@link NamingRule} objects associated with the specified sensor.
     *
     * @param sensor the name of the sensor for which the naming rules are to be retrieved
     * @return a list of {@link NamingRule} objects associated with the given sensor
     */
    List<NamingRule> listBySensor(String sensor);
    /**
     * Retrieves a set of synonym strings associated with the specified sensor.
     *
     * @param sensor the name of the sensor for which synonym strings are to be retrieved
     * @return a set of strings representing synonyms for the specified sensor;
     *         returns an empty set if no synonyms are found
     */
    Set<String> getSynonims(String sensor);

    /**
     * Counts all naming rules.
     */
    long count();
}
