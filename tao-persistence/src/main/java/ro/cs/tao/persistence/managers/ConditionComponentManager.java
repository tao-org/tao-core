package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.component.ConditionComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.enums.ComponentCategory;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.persistence.ConditionComponentProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.repository.ConditionComponentRepository;

import java.util.Optional;

@Configuration
@EnableTransactionManagement
@Component("conditionComponentManager")
public class ConditionComponentManager extends TaoComponentManager<ConditionComponent, ConditionComponentRepository>
        implements ConditionComponentProvider {

    @Override
    public ConditionComponent save(ConditionComponent entity) throws PersistenceException {
        if (!checkEntity(entity, false)) {
            throw new PersistenceException(String.format("Invalid parameters provided for adding new entity of type %s!",
                                                         entity.getClass().getSimpleName()));
        }
        if (entity.getId() != null) {
            final Optional<ConditionComponent> existing = repository.findById(entity.getId());
            if (existing.isPresent()) {
                throw new PersistenceException("There is already another entity with the identifier: " + entity.getId());
            }
        }
        return repository.save(entity);
    }

    @Override
    protected boolean checkEntity(ConditionComponent entity) {
        return checkComponent(entity);
    }

    @Override
    protected boolean checkComponent(TaoComponent component) {
        if (!(component instanceof ConditionComponent)) {
            return false;
        }
        ConditionComponent comp = (ConditionComponent) component;
        return ComponentCategory.CONDITION.equals(comp.getCategory()) &&
                TemplateType.VELOCITY.equals(comp.getTemplateType());
    }
}
