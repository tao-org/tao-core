package ro.cs.tao.persistence.repository;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.repository.PagingAndSortingRepository;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//import ro.cs.tao.persistence.data.User;
//
///**
// * CRUD repository for User entities
// *
// * @author oana
// *
// */
//@Repository
//@Qualifier(value = "userRepository")
//@Transactional
//public interface UserRepository extends PagingAndSortingRepository<User, Integer> {
//
//    /**
//     * Find User entity by its identifier
//     * @param id - the given user identifier
//     * @return the corresponding User entity
//     */
//    User findById(Integer id);
//
//    /**
//     * Find User entity by its username
//     * @param username - the given username
//     * @return the corresponding User entity
//     */
//    User findByUsername(String username);
//}
