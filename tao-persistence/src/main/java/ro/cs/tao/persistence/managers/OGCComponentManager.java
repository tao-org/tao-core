package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.ogc.OGCProcessComponent;
import ro.cs.tao.persistence.OGCComponentProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.repository.OGCProcessComponentRepository;

import java.util.Optional;

@Configuration
@EnableTransactionManagement
@Component("ogcComponentManager")
public class OGCComponentManager extends TaoComponentManager<OGCProcessComponent, OGCProcessComponentRepository>
        implements OGCComponentProvider {

    @Override
    public OGCProcessComponent save(OGCProcessComponent entity) throws PersistenceException {
        if (!checkEntity(entity, false)) {
            throw new PersistenceException(String.format("Invalid parameters provided for adding new entity of type %s!",
                    entity.getClass().getSimpleName()));
        }
        if (entity.getId() != null) {
            final Optional<OGCProcessComponent> existing = repository.findById(entity.getId());
            if (existing.isPresent()) {
                throw new PersistenceException("There is already another entity with the identifier: " + entity.getId());
            }
        }
        return repository.save(entity);
    }

    @Override
    public void updateModified(OGCProcessComponent component) throws PersistenceException {
        this.repository.updateModified(component, component.getId());
    }

    @Override
    protected boolean checkEntity(OGCProcessComponent entity) {
        return checkComponent(entity);
    }

    @Override
    protected boolean checkComponent(TaoComponent component) {
        return component.getLabel() != null && component.getVersion() != null;
    }
}
