package ro.cs.tao.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.cs.tao.gadm.AdminL2Unit;

import java.util.List;

public interface AdminL2UnitRepository extends JpaRepository<AdminL2Unit, String> {
    List<AdminL2Unit> findByAdminL1Id(String adminL1Id);
    AdminL2Unit findByNameIgnoreCase(String name);
}
