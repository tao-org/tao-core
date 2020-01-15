package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.topology.NodeFlavor;

@Repository
@Qualifier(value = "nodeFlavorRepository")
@Transactional
public interface NodeFlavorRepository extends CrudRepository<NodeFlavor, String> {
}
