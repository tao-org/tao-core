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

import java.util.List;

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
