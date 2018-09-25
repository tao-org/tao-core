package ro.cs.tao.persistence.repository;

import org.springframework.jdbc.core.RowMapper;
import ro.cs.tao.Sort;
import ro.cs.tao.SortDirection;
import ro.cs.tao.Tag;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.TagType;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.convert.TagTypeConverter;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.workflow.WorkflowDescriptor;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class TagRepository extends NonMappedRepository<Tag, Long> {

    public TagRepository(PersistenceManager persistenceManager) {
        super(persistenceManager);
    }

    public List<Tag> getComponentTags() throws PersistenceException {
        return getTags(TagType.COMPONENT);
    }

    public List<Tag> getNodeTags() throws PersistenceException {
        return getTags(TagType.TOPOLOGY_NODE);
    }

    public List<Tag> getDatasourceTags() throws PersistenceException {
        return getTags(TagType.DATASOURCE);
    }

    public List<Tag> getWorkflowTags() throws PersistenceException {
        return getTags(TagType.WORKFLOW);
    }

    @Override
    protected RowMapper<Tag> rowMapper() {
        return (resultSet, i) -> {
            Tag tag = new Tag();
            tag.setId(resultSet.getLong(1));
            tag.setTagType(new TagTypeConverter().convertToEntityAttribute(resultSet.getInt(2)));
            tag.setText(resultSet.getString(3));
            return tag;
        };
    }

    @Override
    protected String selectSQL() { return "SELECT id, tag_type_id, tag"; }

    @Override
    protected String insertSQL() { return "INSERT INTO tao.tag (tag_type_id, tag) VALUES (?, ?)"; }

    @Override
    protected String updateSQL() { return "UPDATE tao.tag SET tag_type_id = ?, tag = ?"; }

    @Override
    protected String deleteSQL() { return "DELETE FROM tao.tag"; }

    public Tag addComponentTag(final String componentId, final String tag) throws PersistenceException {
        Tag entity = single(() -> "tag = ?",
                            statement -> {
                                try {
                                    statement.setString(1, tag);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                return statement;
                            });
        if (entity == null) {
            entity = insert("id", Long.class, statement -> {
                                                try {
                                                    statement.setInt(1, TagType.COMPONENT.value());
                                                    statement.setString(2, tag);
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                                return statement;
                                            });
        }
        ProcessingComponent component = persistenceManager.getProcessingComponentById(componentId);
        if (component == null) {
            throw new PersistenceException(String.format("Inexistent component with id '%s'", componentId));
        }
        component.addTag(tag);
        persistenceManager.saveProcessingComponent(component);
        return entity;
    }

    public Tag addDatasourceTag(final String datasourceId, final String tag) throws PersistenceException {
        Tag entity = single(() -> "tag = ?",
                            statement -> {
                                try {
                                    statement.setString(1, tag);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                return statement;
                            });
        if (entity == null) {
            entity = insert("id", Long.class, statement -> {
                try {
                    statement.setInt(1, TagType.DATASOURCE.value());
                    statement.setString(2, tag);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return statement;
            });
        }
        DataSourceComponent component = persistenceManager.getDataSourceInstance(datasourceId);
        if (component == null) {
            throw new PersistenceException(String.format("Inexistent datasource with id '%s'", datasourceId));
        }
        component.addTag(tag);
        persistenceManager.saveDataSourceComponent(component);
        return entity;
    }

    public Tag addWorkflowTag(final long workflowId, final String tag) throws PersistenceException {
        Tag entity = single(() -> "tag = ?",
                            statement -> {
                                try {
                                    statement.setString(1, tag);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                return statement;
                            });
        if (entity == null) {
            entity = insert("id", Long.class, statement -> {
                try {
                    statement.setInt(1, TagType.WORKFLOW.value());
                    statement.setString(2, tag);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return statement;
            });
        }
        WorkflowDescriptor workflow = persistenceManager.getWorkflowDescriptor(workflowId);
        if (workflow == null) {
            throw new PersistenceException(String.format("Inexistent workflow with id '%s'", workflowId));
        }
        workflow.addTag(tag);
        persistenceManager.saveWorkflowDescriptor(workflow);
        return entity;
    }

    public Tag addTopologyNodeTag(final String nodeId, final String tag) throws PersistenceException {
        Tag entity = single(() -> "tag = ?",
                            statement -> {
                                try {
                                    statement.setString(1, tag);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                return statement;
                            });
        if (entity == null) {
            entity = insert("id", Long.class, statement -> {
                try {
                    statement.setInt(1, TagType.TOPOLOGY_NODE.value());
                    statement.setString(2, tag);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return statement;
            });
        }
        NodeDescription node = persistenceManager.getNodeByHostName(nodeId);
        if (node == null) {
            throw new PersistenceException(String.format("Inexistent topology node with id '%s'", nodeId));
        }
        node.addTag(tag);
        persistenceManager.saveExecutionNode(node);
        return entity;
    }

    private List<Tag> getTags(TagType tagType) throws PersistenceException {
        List<Tag> results = list(() -> "tag_type_id = ?",
                                 statement -> {
                                     try {
                                         statement.setInt(1, tagType.value());
                                     } catch (SQLException e) {
                                         e.printStackTrace();
                                     }
                                     return statement;
                                 }, new Sort("tag", SortDirection.ASC));
        return results.stream().distinct().collect(Collectors.toList());
    }
}
