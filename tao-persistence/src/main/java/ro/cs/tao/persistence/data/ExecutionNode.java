package ro.cs.tao.persistence.data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ExecutionNode persistent entity
 *
 * @author oana
 *
 */
@Entity
@Table(name = "tao.execution_node")
public class ExecutionNode implements Serializable {

    /**
     * Execution node IP address column maximum length
     */
    private static final int EXECUTION_NODE_IP_ADDRESS_COLUMN_MAX_LENGTH = 50;

    /**
     * Execution node username column maximum length
     */
    private static final int EXECUTION_NODE_USERNAME_COLUMN_MAX_LENGTH = 50;

    /**
     * Execution node name column maximum length
     */
    private static final int EXECUTION_NODE_NAME_COLUMN_MAX_LENGTH = 250;

    /**
     * Unique identifier
     */
    @Id
    @SequenceGenerator(name = "execution_node_identifier", sequenceName = "tao.execution_node_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "execution_node_identifier")
    @Column(name = "id")
    @NotNull
    private Integer id;

    /**
     * IP address
     */
    @Column(name = "ip_address")
    @NotNull
    @Size(min = 1, max = EXECUTION_NODE_IP_ADDRESS_COLUMN_MAX_LENGTH)
    private String ipAddress;

    /**
     * Username for connecting to the execution node
     */
    @Column(name = "username")
    @NotNull
    @Size(min = 1, max = EXECUTION_NODE_USERNAME_COLUMN_MAX_LENGTH)
    private String username;

    /**
     * Password for connecting to the execution node
     */
    @Column(name = "password")
    @NotNull
    private String password;

    /**
     * Execution node total CPU
     */
    @Column(name = "total_cpu")
    @NotNull
    private Integer totalCPU;

    /**
     * Execution node total RAM
     */
    @Column(name = "total_ram")
    @NotNull
    private Integer totalRAM;

    /**
     * Execution node total HDD
     */
    @Column(name = "total_hdd")
    @NotNull
    private Integer totalHDD;

    /**
     * Execution node name
     */
    @Column(name = "name")
    @NotNull
    @Size(min = 1, max = EXECUTION_NODE_NAME_COLUMN_MAX_LENGTH)
    private String name;

    /**
     * Execution node description
     */
    @Column(name = "description")
    private String description;

    /**
     * Execution node SSH key
     */
    @Column(name = "ssh_key")
    private String sshKey;

    /**
     * Execution node used CPU
     */
    @Column(name = "used_cpu")
    private Integer usedCPU;

    /**
     * Execution node used RAM
     */
    @Column(name = "used_ram")
    private Integer usedRAM;

    /**
     * Execution node used HDD
     */
    @Column(name = "used_hdd")
    private Integer usedHDD;

    /**
     * Created date
     */
    @Column(name = "created")
    @NotNull
    private LocalDateTime createdDate;

    /**
     * Modified date
     */
    @Column(name = "modified")
    private LocalDateTime modifiedDate;

    /**
     * Flag that indicates if the execution node is active or not
     */
    @Column(name = "active")
    @NotNull
    private Boolean active;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getTotalCPU() {
        return totalCPU;
    }

    public void setTotalCPU(Integer totalCPU) {
        this.totalCPU = totalCPU;
    }

    public Integer getTotalRAM() {
        return totalRAM;
    }

    public void setTotalRAM(Integer totalRAM) {
        this.totalRAM = totalRAM;
    }

    public Integer getTotalHDD() {
        return totalHDD;
    }

    public void setTotalHDD(Integer totalHDD) {
        this.totalHDD = totalHDD;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }

    public Integer getUsedCPU() {
        return usedCPU;
    }

    public void setUsedCPU(Integer usedCPU) {
        this.usedCPU = usedCPU;
    }

    public Integer getUsedRAM() {
        return usedRAM;
    }

    public void setUsedRAM(Integer usedRAM) {
        this.usedRAM = usedRAM;
    }

    public Integer getUsedHDD() {
        return usedHDD;
    }

    public void setUsedHDD(Integer usedHDD) {
        this.usedHDD = usedHDD;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
