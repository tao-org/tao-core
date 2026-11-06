package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.gadm.AdminL1Unit;
import ro.cs.tao.persistence.AdminL1UnitProvider;
import ro.cs.tao.persistence.repository.AdminL1UnitRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("adminL1UnitManager")
public class AdminL1UnitManager extends EntityManager<AdminL1Unit, String, AdminL1UnitRepository>
        implements AdminL1UnitProvider {

    @Transactional
    public List<AdminL1Unit> getByCountryCode(String countryCode) {
        return repository.findByCountryCode(countryCode);
    }

    @Override
    public AdminL1Unit getByName(String name) {
        return repository.findByNameIgnoreCase(name);
    }

    @Override
    public void saveAll(List<AdminL1Unit> units) {
        repository.saveAll(units);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(AdminL1Unit entity) {
        return entity != null
                && !StringUtilities.isNullOrEmpty(entity.getId())
                && !StringUtilities.isNullOrEmpty(entity.getName())
                && !StringUtilities.isNullOrEmpty(entity.getCountryCode())
                && entity.getCountryCode().length() == 3
                && entity.getExtent() != null;
    }
}
