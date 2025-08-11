package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.ogc.OGCProcessComponent;

@Repository
@Qualifier(value = "ogcComponentRepository")
@Transactional
public interface OGCProcessComponentRepository extends AuditableEntityRepository<OGCProcessComponent, String> {

}
