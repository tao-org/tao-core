package ro.cs.tao.persistence;

import ro.cs.tao.gadm.AdminL2Unit;

import java.util.List;

public interface AdminL2UnitProvider extends EntityProvider<AdminL2Unit, String> {
    List<AdminL2Unit> getByAdminL1Id(String adminL1Id);
    AdminL2Unit getByName(String name);
    void saveAll(List<AdminL2Unit> units);
}
