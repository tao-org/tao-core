package ro.cs.tao.persistence.repository;

import jakarta.persistence.QueryHint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.configuration.UserConfigurationItem;
import ro.cs.tao.configuration.UserConfigurationItemId;

import java.util.List;

@Repository
@Qualifier(value = "userConfigurationRepository")
@Transactional
public interface UserConfigurationRepository extends AuditableEntityRepository<UserConfigurationItem, UserConfigurationItemId> {

    @Query(value = "SELECT * FROM config.user_config WHERE user_id = :userId ORDER BY id", nativeQuery = true)
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true"), @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    List<UserConfigurationItem> getByUser(@Param("userId") String userId);

    @Query(value = "SELECT * FROM config.user_config WHERE user_id = ?1 AND id LIKE ?2 ||'%' ORDER BY id", nativeQuery = true)
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true"), @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    List<UserConfigurationItem> getByUserStartingWith(String userId, String filter);

    @Query(value = "SELECT * FROM config.user_config WHERE user_id = ?1 AND id = ?2 ORDER BY id", nativeQuery = true)
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true"), @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    UserConfigurationItem getItem(String userId, String key);
}

