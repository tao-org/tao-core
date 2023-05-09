package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.Tag;
import ro.cs.tao.component.enums.TagType;
import ro.cs.tao.persistence.TagProvider;
import ro.cs.tao.persistence.repository.TagRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("tagManager")
public class TagManager extends EntityManager<Tag, Long, TagRepository> implements TagProvider {

    @Override
    public List<Tag> list(TagType type) {
        return repository.getTags(type.value());
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(Tag entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getText()) && entity.getTagType() != null;
    }
}
