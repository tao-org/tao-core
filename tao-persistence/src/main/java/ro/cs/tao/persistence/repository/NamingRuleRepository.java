package ro.cs.tao.persistence.repository;

import jakarta.persistence.QueryHint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.eodata.naming.NamingRule;

import java.util.List;

@Repository
@Qualifier(value = "namingRuleRepository")
@Transactional
public interface NamingRuleRepository extends JpaRepository<NamingRule, Integer> {

    @Query(value = "SELECT * FROM product.naming_rule WHERE sensor = ?1 OR COALESCE(synonyms,'') LIKE '%' || ?1 || '%'", nativeQuery = true)
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true"), @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    List<NamingRule> getBySensor(@Param("sensor") String sensor);

}

