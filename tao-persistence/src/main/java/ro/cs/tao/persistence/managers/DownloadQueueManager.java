package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.datasource.DownloadQueueItem;
import ro.cs.tao.datasource.DownloadQueuePersister;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.persistence.repository.DownloadQueueRepository;
import ro.cs.tao.utils.Crypto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("downloadQueueManager")
public class DownloadQueueManager
        extends EntityManager<DownloadQueueItem, String, DownloadQueueRepository>
        implements DownloadQueuePersister {
    @Override
    public DownloadQueueItem load(String id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void remove(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<DownloadQueueItem> restore() {
        final List<DownloadQueueItem> results = new ArrayList<>();
        repository.findAll().forEach(results::add);
        return results;
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(DownloadQueueItem entity) {
        if (entity == null) {
            return false;
        }
        List<String> strings = entity.getProducts().stream().map(EOProduct::getId).collect(Collectors.toList());
        strings.add(entity.getDataSourceName());
        strings.add(entity.getDestinationPath());
        return entity.getId() == null || Crypto.hash(strings).equals(entity.getId());
    }
}
