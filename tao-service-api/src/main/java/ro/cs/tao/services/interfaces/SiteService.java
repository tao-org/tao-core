package ro.cs.tao.services.interfaces;

import ro.cs.tao.gadm.AdminL1Unit;
import ro.cs.tao.gadm.AdminL2Unit;
import ro.cs.tao.gadm.Country;
import ro.cs.tao.workspaces.Site;

import java.util.List;

public interface SiteService extends CRUDService<Site, String> {

    List<Site> getByUser(String userId);

    List<Country> getCountries();

    Country getCountryByName(String name);

    Country getCountryByCode(String isoCode);

    void saveCountries(List<Country> countries);

    List<AdminL1Unit> getAdminL1Units(String countryCode);

    AdminL1Unit getAdminL1UnitByName(String name);

    void updateCountry(String countryCode, List<AdminL1Unit> units);

    List<AdminL2Unit> getAdminL2Units(String adminL1Id);

    AdminL2Unit getAdminL2UnitByName(String name);

    void updateAdminL1(String adminL1Id, List<AdminL2Unit> units);

    void saveAdminL2Units(List<AdminL2Unit> units);
}
