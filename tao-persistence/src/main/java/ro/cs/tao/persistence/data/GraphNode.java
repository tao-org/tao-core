package ro.cs.tao.persistence.data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * GraphNode persistent entity
 *
 * @author oana
 *
 */
@Entity
@Table(name = "tao.graph_node")
public class GraphNode {

    /**
     * Graph node name column maximum length
     */
    private static final int GRAPH_NODE_NAME_COLUMN_MAX_LENGTH = 250;

    /**
     * Unique identifier
     */
    @Id
    @SequenceGenerator(name = "graph_node_identifier", sequenceName = "tao.graph_node_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "graph_node_identifier")
    @Column(name = "id")
    @NotNull
    private Long id;

    /**
     * Graph node name
     */
    @Column(name = "name")
    @NotNull
    @Size(min = 1, max = GRAPH_NODE_NAME_COLUMN_MAX_LENGTH)
    private String name;

    /**
     * The workflow graph to which this node belongs to
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowGraph workflowGraph;

    /**
     * Node position X coordinate
     */
    @Column(name = "xcoord")
    @NotNull
    private Double xCoord;

    /**
     * Node position Y coordinate
     */
    @Column(name = "ycoord")
    @NotNull
    private Double yCoord;

    /**
     * The workflow graph node origin (the node preceding this node)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "origin", nullable = true)
    private GraphNode origin;

    /**
     * The workflow graph node destination (the node following this node)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination", nullable = true)
    private GraphNode destination;

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

    public WorkflowGraph getWorkflowGraph() {
        return workflowGraph;
    }

    public void setWorkflowGraph(WorkflowGraph workflowGraph) {
        this.workflowGraph = workflowGraph;
    }

    public Double getxCoord() {
        return xCoord;
    }

    public void setxCoord(Double xCoord) {
        this.xCoord = xCoord;
    }

    public Double getyCoord() {
        return yCoord;
    }

    public void setyCoord(Double yCoord) {
        this.yCoord = yCoord;
    }

    public GraphNode getOrigin() {
        return origin;
    }

    public void setOrigin(GraphNode origin) {
        this.origin = origin;
    }

    public GraphNode getDestination() {
        return destination;
    }

    public void setDestination(GraphNode destination) {
        this.destination = destination;
    }
}
