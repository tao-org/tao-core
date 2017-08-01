package ro.cs.tao.persistence.data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Created by oana on 7/27/2017.
 */
@Entity
@Table(name = "tao.job")
public class Job {

    /**
     * Unique identifier
     */
    @Id
    @SequenceGenerator(name = "job_identifier", sequenceName = "tao.job_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_identifier")
    @Column(name = "id")
    @NotNull
    private Long id;

    /**
     * Start time
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * End time
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * The user launching this job
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    /**
     * The corresponding workflow for this job (execution)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowGraph workflowGraph;

    /**
     * Job execution status
     */
    @Column(name = "job_status_id")
    private Integer status;

    /**
     * Job tasks
     */
    @OneToMany (fetch = FetchType.EAGER, mappedBy = "job")
    private Set<Task> tasks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WorkflowGraph getWorkflowGraph() {
        return workflowGraph;
    }

    public void setWorkflowGraph(WorkflowGraph workflowGraph) {
        this.workflowGraph = workflowGraph;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }
}
