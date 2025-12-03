package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.component.PassThroughComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.enums.ComponentCategory;
import ro.cs.tao.persistence.PassThroughComponentProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.repository.PassThroughComponentRepository;

import java.util.Optional;

@Configuration
@EnableTransactionManagement
@Component("passThroughComponentManager")
public class PassThroughComponentManager extends TaoComponentManager<PassThroughComponent, PassThroughComponentRepository>
        implements PassThroughComponentProvider {

    @Override
    public PassThroughComponent save(PassThroughComponent entity) throws PersistenceException {
        if (!checkEntity(entity, false)) {
            throw new PersistenceException(String.format("Invalid parameters provided for adding new entity of type %s!",
                                                         entity.getClass().getSimpleName()));
        }
        if (entity.getId() != null) {
            final Optional<PassThroughComponent> existing = repository.findById(entity.getId());
            if (existing.isPresent()) {
                throw new PersistenceException("There is already another entity with the identifier: " + entity.getId());
            }
        }
        return repository.save(entity);
    }

    @Override
    public void updateModified(PassThroughComponent component) throws PersistenceException {
        this.repository.updateModified(component, component.getId());
    }

    @Override
    protected boolean checkEntity(PassThroughComponent entity) {
        return checkComponent(entity);
    }

    @Override
    protected boolean checkComponent(TaoComponent component) {
        if (!(component instanceof PassThroughComponent)) {
            return false;
        }
        PassThroughComponent comp = (PassThroughComponent) component;
        return ComponentCategory.PASSTHROUGH.equals(comp.getCategory());
    }
}
