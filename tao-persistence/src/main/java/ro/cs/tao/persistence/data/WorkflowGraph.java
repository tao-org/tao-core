package ro.cs.tao.persistence.data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * WorkflowGraph persistent entity
 *
 * @author oana
 *
 */
@Entity
@Table(name = "tao.workflow_graph")
public class WorkflowGraph {

    /**
     * Workflow graph name column maximum length
     */
    private static final int WORKFLOW_GRAPH_NAME_COLUMN_MAX_LENGTH = 250;

    /**
     * Workflow definition path column maximum length
     */
    private static final int WORKFLOW_DEFINITION_PATH_COLUMN_MAX_LENGTH = 512;

    /**
     * Unique identifier
     */
    @Id
    @SequenceGenerator(name = "workflow_graph_identifier", sequenceName = "tao.workflow_graph_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_graph_identifier")
    @Column(name = "id")
    @NotNull
    private Long id;

    /**
     * Workflow graph name
     */
    @Column(name = "name")
    @NotNull
    @Size(min = 1, max = WORKFLOW_GRAPH_NAME_COLUMN_MAX_LENGTH)
    private String name;

    /**
     * Created date
     */
    @Column(name = "created")
    @NotNull
    private LocalDateTime createdDate;

    /**
     * The user to which this product belongs to
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Workflow definition path
     */
    @Column(name = "definition_path")
    @NotNull
    @Size(min = 1, max = WORKFLOW_DEFINITION_PATH_COLUMN_MAX_LENGTH)
    private String definitionPath;

    /**
     * Workflow visibility
     */
    @Column(name = "visibility_id")
    @NotNull
    private Integer visibility;

    /**
     * Flag that indicates if the workflow graph is active or not
     */
    @Column(name = "active")
    @NotNull
    private Boolean active;

    /**
      * Workflow graph nodes
    */
    @OneToMany (fetch = FetchType.EAGER, mappedBy = "workflowGraph")
    private Set<GraphNode> graphNodes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDefinitionPath() {
        return definitionPath;
    }

    public void setDefinitionPath(String definitionPath) {
        this.definitionPath = definitionPath;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<GraphNode> getGraphNodes() {
        return graphNodes;
    }

    public void setGraphNodes(Set<GraphNode> graphNodes) {
        this.graphNodes = graphNodes;
    }
}
