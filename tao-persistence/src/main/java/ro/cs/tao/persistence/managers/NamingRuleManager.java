package ro.cs.tao.persistence.managers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.eodata.naming.NameExpressionParser;
import ro.cs.tao.eodata.naming.NamingRule;
import ro.cs.tao.persistence.NamingRuleProvider;
import ro.cs.tao.persistence.repository.NamingRuleRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("namingRuleManager")
public class NamingRuleManager extends EntityManager<NamingRule, Integer, NamingRuleRepository>
                                implements NamingRuleProvider {

    @Override
    public List<NamingRule> listBySensor(String sensor) {
        final String search = "n/a".equalsIgnoreCase(sensor) ? "Generic" : sensor;
        return repository.getBySensor(search);
    }

    @Override
    public Set<String> getSynonims(String sensor) {
        final List<NamingRule> rules = listBySensor(sensor);
        Set<String> synonims;
        if (rules == null || rules.isEmpty()) {
            synonims = Set.of();
        } else {
            synonims = new HashSet<>();
            for (NamingRule rule : rules) {
                synonims.add(rule.getSensor());
                if (!StringUtilities.isNullOrEmpty(rule.getSynonyms())) {
                    String[] syns = rule.getSynonyms().split(",");
                    for (String syn : syns) {
                        synonims.add(syn);
                    }
                }
            }
        }
        return synonims;
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(Integer entityId, boolean existingEntity) {
        return entityId != null && (existingEntity == (get(entityId) != null));
    }

    @Override
    protected boolean checkEntity(NamingRule rule) {
        try {
            NameExpressionParser parser = new NameExpressionParser(rule);
        } catch (Exception e) {
            return false;
        }
        return rule != null && StringUtils.isNotBlank(rule.getRegEx()) &&
                StringUtils.isNotBlank(rule.getSensor()) &&
                StringUtils.isNotBlank(rule.getDescription());
    }
}
