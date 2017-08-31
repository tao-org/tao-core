package ro.cs.tao.persistence.data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Task persistent entity
 *
 * @author oana
 *
 */
@Entity
@Table(name = "tao.task")
public class Task implements Serializable {
    /**
     * Unique identifier
     */
    @Id
    @SequenceGenerator(name = "task_identifier", sequenceName = "tao.task_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_identifier")
    @Column(name = "id")
    @NotNull
    private Long id;

    /**
     * The corresponding processing component for this task
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "processing_component_id", nullable = false)
    private ProcessingComponent processingComponent;

    /**
     * The corresponding workflow graph node for this task
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "graph_node_id", nullable = false)
    private GraphNode graphNode;

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
     * The job to which this task belongs to
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /**
     * Task execution status
     */
    @Column(name = "task_status_id")
    private Integer status;

    /**
     * Task inputs
     */
    @OneToMany (fetch = FetchType.EAGER, mappedBy = "task")
    private Set<TaskInput> inputs;

    /**
     * Task outputs
     */
    @OneToMany (fetch = FetchType.EAGER, mappedBy = "task")
    private Set<TaskOutput> outputs;

    /**
     * Corresponding execution node
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="execution_node_id", nullable = false)
    private ExecutionNode executionNode;

    /**
     * Task resources
     */
    /**
     * Task used CPU
     */
    @Column(name = "used_cpu")
    private Integer usedCPU;

    /**
     * Task used RAM
     */
    @Column(name = "used_ram")
    private Integer usedRAM;

    /**
     * Task used HDD
     */
    @Column(name = "used_hdd")
    private Integer usedHDD;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProcessingComponent getProcessingComponent() {
        return processingComponent;
    }

    public void setProcessingComponent(ProcessingComponent processingComponent) {
        this.processingComponent = processingComponent;
    }

    public GraphNode getGraphNode() {
        return graphNode;
    }

    public void setGraphNode(GraphNode graphNode) {
        this.graphNode = graphNode;
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

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Set<TaskInput> getInputs() {
        return inputs;
    }

    public void setInputs(Set<TaskInput> inputs) {
        this.inputs = inputs;
    }

    public Set<TaskOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(Set<TaskOutput> outputs) {
        this.outputs = outputs;
    }

    public ExecutionNode getExecutionNode() {
        return executionNode;
    }

    public void setExecutionNode(ExecutionNode executionNode) {
        this.executionNode = executionNode;
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
}
