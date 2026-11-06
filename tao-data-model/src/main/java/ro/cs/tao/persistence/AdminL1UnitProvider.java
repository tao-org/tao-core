package ro.cs.tao.persistence;

import ro.cs.tao.gadm.AdminL1Unit;

import java.util.List;

public interface AdminL1UnitProvider extends EntityProvider<AdminL1Unit, String> {
    List<AdminL1Unit> getByCountryCode(String countryCode);
    AdminL1Unit getByName(String name);
    void saveAll(List<AdminL1Unit> units);
}
