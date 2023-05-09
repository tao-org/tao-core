package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.AuditProvider;
import ro.cs.tao.persistence.repository.AuditRepository;
import ro.cs.tao.user.LogEvent;
import ro.cs.tao.utils.StringUtilities;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("auditManager")
public class AuditManager extends EntityManager<LogEvent, Long, AuditRepository> implements AuditProvider {

    @Override
    public List<LogEvent> getByUser(String user) {
        return repository.getByUser(user);
    }

    @Override
    public List<LogEvent> getByUserAndInterval(String user, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.getByUserAndInterval(user, startDate, endDate);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(Long entityId, boolean existingEntity) {
        return true;
    }

    @Override
    protected boolean checkEntity(LogEvent data) {
        return data != null && !StringUtilities.isNullOrEmpty(data.getUserName())
                && !StringUtilities.isNullOrEmpty(data.getEvent());
    }
}
