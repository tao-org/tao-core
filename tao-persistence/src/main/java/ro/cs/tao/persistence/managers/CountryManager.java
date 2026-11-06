package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.gadm.Country;
import ro.cs.tao.persistence.CountryProvider;
import ro.cs.tao.persistence.repository.CountryRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("countryManager")
public class CountryManager extends EntityManager<Country, String, CountryRepository>
        implements CountryProvider {

    @Transactional
    public List<Country> listEnabled() {
        return repository.findByEnabledTrue();
    }

    @Override
    public Country getByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public Country getByIso3Code(String iso3Code) {
        return repository.findByIso3Code(iso3Code);
    }

    @Override
    public void saveAll(List<Country> countries) {
        this.repository.saveAll(countries);
    }

    @Override
    protected String identifier() {
        return "iso3Code";
    }

    @Override
    protected boolean checkEntity(Country entity) {
        return entity != null
                && !StringUtilities.isNullOrEmpty(entity.getIso3Code())
                && entity.getIso3Code().length() == 3
                && !StringUtilities.isNullOrEmpty(entity.getName())
                && entity.getLatitude() != null
                && entity.getLongitude() != null
                && entity.getExtent() != null;
    }
}
