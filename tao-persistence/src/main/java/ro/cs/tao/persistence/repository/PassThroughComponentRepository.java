package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.PassThroughComponent;

@Repository
@Qualifier(value = "passThroughComponentRepository")
@Transactional
public interface PassThroughComponentRepository  extends AuditableEntityRepository<PassThroughComponent, String> {
}
