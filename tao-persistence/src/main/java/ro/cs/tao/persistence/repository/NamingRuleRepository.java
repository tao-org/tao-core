package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.eodata.naming.NamingRule;

import java.util.List;

@Repository
@Qualifier(value = "namingRuleRepository")
@Transactional
public interface NamingRuleRepository extends PagingAndSortingRepository<NamingRule, Integer> {

    List<NamingRule> findBySensor(String sensor);

}