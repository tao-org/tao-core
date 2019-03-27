package ro.cs.tao.persistence.managers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.eodata.naming.NameExpressionParser;
import ro.cs.tao.eodata.naming.NamingRule;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.NamingRuleRepository;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("namingRuleManager")
public class NamingRuleManager {

    @Autowired
    private NamingRuleRepository namingRuleRepository;

    public NamingRule findById(int id) {
        return namingRuleRepository.findById(id).orElse(null);
    }

    public List<NamingRule> findBySensor(String sensor) {
        return namingRuleRepository.findBySensor(sensor);
    }

    public List<NamingRule> list() {
        return (List<NamingRule>) namingRuleRepository.findAll();
    }

    public NamingRule save(NamingRule rule) throws PersistenceException {
        if (!checkEntity(rule)) {
            throw new PersistenceException("Invalid values provided for saving NamingRule");
        }
        return namingRuleRepository.save(rule);
    }

    public void delete(int ruleId) {
        namingRuleRepository.deleteById(ruleId);
    }

    public void delete(NamingRule rule) {
        namingRuleRepository.delete(rule);
    }

    private boolean checkEntity(NamingRule rule) {
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
