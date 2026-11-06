package ro.cs.tao.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.cs.tao.gadm.AdminL1Unit;

import java.util.List;

public interface AdminL1UnitRepository extends JpaRepository<AdminL1Unit, String> {
    List<AdminL1Unit> findByCountryCode(String countryCode);
    AdminL1Unit findByNameIgnoreCase(String name);
}
