package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.ParameterExpansionRule;

@Repository
@Qualifier(value = "expansionRuleRepository")
@Transactional
public interface ParameterExpansionRuleRepository extends CrudRepository<ParameterExpansionRule, String> {
}
