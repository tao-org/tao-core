package ro.cs.tao.persistence.repository;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.repository.PagingAndSortingRepository;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//import ro.cs.tao.persistence.data.Group;
//
///**
// * CRUD repository for Group entities
// *
// * @author oana
// *
// */
//@Repository
//@Qualifier(value = "groupRepository")
//@Transactional
//public interface GroupRepository extends PagingAndSortingRepository<Group, Integer> {
//    /**
//     * Find Group entity by its identifier
//     * @param id - the given group identifier
//     * @return the corresponding Group entity
//     */
//    Group findById(Integer id);
//
//    /**
//     * Find Group entity by its name
//     * @param name - the given group name
//     * @return the corresponding Group entity
//     */
//    Group findByName(String name);
//}
