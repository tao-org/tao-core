package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.eodata.AuxiliaryData;
import ro.cs.tao.persistence.AuxiliaryDataProvider;
import ro.cs.tao.persistence.repository.AuxDataRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("auxiliaryDataManager")
public class AuxiliaryDataManager extends EntityManager<AuxiliaryData, String, AuxDataRepository> implements AuxiliaryDataProvider {

    @Override
    public List<AuxiliaryData> list(String userName) {
        return repository.getAuxiliaryDataByUser(userName);
    }

    @Override
    public List<AuxiliaryData> list(String userName, String... locations) {
        return repository.getAuxiliaryDataByLocation(userName, new HashSet<>(Arrays.asList(locations)));
    }

    @Override
    public AuxiliaryData getByLocation(String location) {
        return repository.getByLocation(location);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && (existingEntity == (get(entityId) != null));
    }

    @Override
    protected boolean checkEntity(AuxiliaryData auxiliaryData) {
        return auxiliaryData != null && auxiliaryData.getLocation() != null && !auxiliaryData.getLocation().isEmpty() &&
                auxiliaryData.getDescription() != null && !auxiliaryData.getDescription().isEmpty() &&
                auxiliaryData.getUserName() != null && !auxiliaryData.getUserName().isEmpty();
    }
}
