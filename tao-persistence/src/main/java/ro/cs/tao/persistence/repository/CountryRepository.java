package ro.cs.tao.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.cs.tao.gadm.Country;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, String> {
    List<Country> findByEnabledTrue();
    Country findByName(String name);
    Country findByIso3Code(String iso3Code);
}
