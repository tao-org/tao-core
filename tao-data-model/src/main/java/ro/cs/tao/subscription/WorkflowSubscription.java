package ro.cs.tao.subscription;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import ro.cs.tao.component.LongIdentifiable;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

/**
 * Descriptor for a workflow subscription.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "workflowSubscription")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = WorkflowSubscription.class)
public class WorkflowSubscription extends LongIdentifiable {

    private String userId;
    private long workflowId;
    private LocalDateTime created;

    @Override
    public Long defaultId() { return 0L; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(long workflowId) {
        this.workflowId = workflowId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
