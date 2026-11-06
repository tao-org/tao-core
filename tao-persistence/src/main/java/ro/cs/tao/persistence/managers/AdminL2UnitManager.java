package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.gadm.AdminL2Unit;
import ro.cs.tao.persistence.AdminL2UnitProvider;
import ro.cs.tao.persistence.repository.AdminL2UnitRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("adminL2UnitManager")
public class AdminL2UnitManager extends EntityManager<AdminL2Unit, String, AdminL2UnitRepository>
        implements AdminL2UnitProvider {

    @Transactional
    public List<AdminL2Unit> getByAdminL1Id(String adminL1Id) {
        return repository.findByAdminL1Id(adminL1Id);
    }

    @Override
    public AdminL2Unit getByName(String name) {
        return repository.findByNameIgnoreCase(name);
    }

    @Override
    public void saveAll(List<AdminL2Unit> units) {
        repository.saveAll(units);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(AdminL2Unit entity) {
        return entity != null
                && !StringUtilities.isNullOrEmpty(entity.getId())
                && !StringUtilities.isNullOrEmpty(entity.getName())
                && !StringUtilities.isNullOrEmpty(entity.getAdminL1Id())
                && entity.getExtent() != null;
    }
}
