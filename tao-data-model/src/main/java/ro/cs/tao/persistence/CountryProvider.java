package ro.cs.tao.persistence;

import ro.cs.tao.gadm.Country;

import java.util.List;

public interface CountryProvider extends EntityProvider<Country, String> {
    List<Country> listEnabled();
    Country getByName(String name);
    Country getByIso3Code(String iso3Code);
    void saveAll(List<Country> countries);
}
