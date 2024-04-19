package ro.cs.tao.subscription;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import ro.cs.tao.component.LongIdentifiable;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

/**
 * Descriptor for a dataset subscription.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "dataSubscription")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = DataSubscription.class)
public class DataSubscription extends LongIdentifiable {

    private String userId;
    private String repositoryId;
    private String name;
    private String dataRootPath;
    private String checkSum;
    private int subscribersCount;
    private LocalDateTime created;

    @Override
    public Long defaultId() { return 0L; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataRootPath() {
        return dataRootPath;
    }

    public void setDataRootPath(String dataRootPath) {
        this.dataRootPath = dataRootPath;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public int getSubscribersCount() {
        return subscribersCount;
    }

    public void setSubscribersCount(int subscribersCount) {
        this.subscribersCount = subscribersCount;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
