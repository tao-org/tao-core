package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.ogc.WMSComponent;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.WMSComponentProvider;
import ro.cs.tao.persistence.repository.WMSComponentRepository;

import java.util.Optional;
@Configuration
@EnableTransactionManagement
@Component("wmsComponentManager")
public class WMSComponentManager extends TaoComponentManager<WMSComponent, WMSComponentRepository>
        implements WMSComponentProvider {

    @Override
    public WMSComponent save(WMSComponent entity) throws PersistenceException {
        if (!checkEntity(entity, false)) {
            throw new PersistenceException(String.format("Invalid parameters provided for adding new entity of type %s!",
                    entity.getClass().getSimpleName()));
        }
        if (entity.getId() != null) {
            final Optional<WMSComponent> existing = repository.findById(entity.getId());
            if (existing.isPresent()) {
                throw new PersistenceException("There is already another entity with the identifier: " + entity.getId());
            }
        }
        return repository.save(entity);
    }

    @Override
    protected boolean checkEntity(WMSComponent entity) {
        return checkComponent(entity);
    }

    @Override
    protected boolean checkComponent(TaoComponent component) {
        return component.getLabel() != null && component.getVersion() != null;
    }
}
