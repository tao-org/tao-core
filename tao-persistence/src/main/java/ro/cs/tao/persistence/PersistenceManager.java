/*
 * Copyright (C) 2017 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.GroupComponent;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.docker.Container;
import ro.cs.tao.eodata.AuxiliaryData;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.VectorData;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.MessagePersister;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.managers.*;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.ServiceDescription;
import ro.cs.tao.user.Group;
import ro.cs.tao.user.User;
import ro.cs.tao.user.UserPreference;
import ro.cs.tao.user.UserStatus;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DAO
 * Created by Oana H. on 7/18/2017.
 */

@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Scope("singleton")
public class PersistenceManager implements MessagePersister {

    private Log logger = LogFactory.getLog(PersistenceManager.class);

    @Autowired
    private ProductManager productManager;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private ComponentManager componentManager;
    private SimpleCache.Cache<String, ProcessingComponent> componentCache;

    @Autowired
    private WorkflowManager workflowManager;
    private SimpleCache.Cache<Long, WorkflowNodeDescriptor> workflowNodeCache;

    @Autowired
    private ExecutionManager executionManager;

    @Autowired
    private QueryManager queryManager;

    @Autowired
    private NotificationManager notificationManager;

    @Autowired
    private UserManager userManager;

    @PostConstruct
    public void initialize() {
        componentCache = SimpleCache.create(String.class, ProcessingComponent.class,
                                            id -> {
                                                try {
                                                    return componentManager.getProcessingComponentById(id);
                                                } catch (PersistenceException e) {
                                                    return null;
                                                }
                                            });
        workflowNodeCache = SimpleCache.create(Long.class, WorkflowNodeDescriptor.class,
                                               workflowManager::getWorkflowNodeById);
    }

    //region EOProduct and VectorData
    public List<EOProduct> getEOProducts() {
        return productManager.getEOProducts();
    }

    public List<EOProduct> getEOProducts(String...locations) {
        if (locations == null || locations.length == 0) {
            return getEOProducts();
        } else if (locations.length == 1) {
            return productManager.getEOProducts(locations[0]);
        } else {
            Set<String> set = new HashSet<>();
            Collections.addAll(set, locations);
            return productManager.getEOProducts(set);
        }
    }

    public List<EOProduct> getPublicProducts() { return productManager.getPublicEOProducts(); }

    public List<VectorData> getVectorDataProducts() {
        return productManager.getVectorDataProducts();
    }

    public List<VectorData> getVectorDataProducts(String...locations) {
        if (locations == null || locations.length == 0) {
            return getVectorDataProducts();
        } else {
            Set<String> set = new HashSet<>();
            Collections.addAll(set, locations);
            return productManager.getVectorDataProducts(set);
        }
    }

    public List<AuxiliaryData> getAuxiliaryData(String userName) {
        return productManager.getAuxiliaryData(userName);
    }

    public List<AuxiliaryData> getAuxiliaryData(String userName, String...locations) {
        if (locations == null || locations.length == 0) {
            return getAuxiliaryData(userName);
        } else {
            Set<String> set = new HashSet<>();
            Collections.addAll(set, locations);
            return productManager.getAuxiliaryData(userName, set);
        }
    }

    public EOProduct saveEOProduct(EOProduct eoProduct) throws PersistenceException {
        return productManager.saveEOProduct(eoProduct);
    }

    public void remove(EOProduct product) throws PersistenceException {
        productManager.removeProduct(product);
    }

    public VectorData saveVectorDataProduct(VectorData vectorDataProduct) throws PersistenceException {
        return productManager.saveVectorDataProduct(vectorDataProduct);
    }

    public void remove(VectorData product) throws PersistenceException {
        productManager.removeProduct(product);
    }

    public AuxiliaryData saveAuxiliaryData(AuxiliaryData auxiliaryData) throws PersistenceException {
        return productManager.saveAuxiliaryData(auxiliaryData);
    }

    public void removeAuxiliaryData(String location) {
        productManager.removeAuxiliaryData(location);
    }

    public void removeAuxiliaryData(AuxiliaryData auxiliaryData) {
        productManager.removeAuxiliaryData(auxiliaryData);
    }
    //endregion

    //region NodeDescription and ServiceDescription
    public List<NodeDescription> getNodes() {
        return nodeManager.getNodes();
    }

    public NodeDescription getNodeByHostName(String hostName) throws PersistenceException {
        return nodeManager.getNodeByHostName(hostName);
    }

    public NodeDescription saveExecutionNode(NodeDescription node) throws PersistenceException {
        return nodeManager.saveExecutionNode(node);
    }

    public NodeDescription updateExecutionNode(NodeDescription node) throws PersistenceException {
        return nodeManager.updateExecutionNode(node);
    }

    public NodeDescription deleteExecutionNode(String hostName) throws PersistenceException {
        return nodeManager.deleteExecutionNode(hostName);
    }

    public void removeExecutionNode(String hostName) {
        nodeManager.removeExecutionNode(hostName);
    }

    public ServiceDescription saveServiceDescription(ServiceDescription service) throws PersistenceException {
        return nodeManager.saveServiceDescription(service);
    }

    public boolean checkIfExistsNodeByHostName(String hostName) {
        return nodeManager.checkIfExistsNodeByHostName(hostName);
    }
    //endregion

    //region Container
    public List<Container> getContainers() {
        return componentManager.getContainers();
    }

    public Container getContainerById(String id) throws PersistenceException {
        return componentManager.getContainerById(id);
    }

    public Container saveContainer(Container container) throws PersistenceException {
        return componentManager.saveContainer(container);
    }

    public Container updateContainer(Container container) throws PersistenceException {
        return componentManager.updateContainer(container);
    }

    public boolean checkIfExistsContainerById(String id) throws PersistenceException {
        return componentManager.checkIfExistsContainerById(id);
    }

    public void deleteContainer(String id) throws PersistenceException {
        componentManager.deleteContainer(id);
    }
    //endregion

    //region ProcessingComponent
    public List<ProcessingComponent> getProcessingComponents() {
        return componentManager.getProcessingComponents();
    }

    public List<ProcessingComponent> getUserProcessingComponents(String userName) {
        return componentManager.getUserProcessingComponents(userName);
    }

    public List<ProcessingComponent> getUserScriptComponents(String userName) {
        return componentManager.getUserScriptComponents(userName);
    }

    public ProcessingComponent getProcessingComponentById(String id) {//throws PersistenceException {
        return componentCache.get(id); //componentManager.getProcessingComponentById(id);
    }


    public ProcessingComponent saveProcessingComponent(ProcessingComponent component) throws PersistenceException {
        ProcessingComponent c = componentManager.saveProcessingComponent(component);
        componentCache.put(c.getId(), c);
        return c;
    }


    public ProcessingComponent updateProcessingComponent(ProcessingComponent component) throws PersistenceException {
        ProcessingComponent c = componentManager.updateProcessingComponent(component);
        componentCache.put(c.getId(), c);
        return c;
    }

    public boolean checkIfExistsComponentById(String id) {
        return componentManager.checkIfExistsComponentById(id);
    }

    public ProcessingComponent deleteProcessingComponent(String id) throws PersistenceException {
        componentCache.remove(id);
        return componentManager.deleteProcessingComponent(id);
    }
    //endregion

    //region GroupComponent
    public List<GroupComponent> getGroupComponents() {
        return componentManager.getGroupComponents();
    }

    public GroupComponent getGroupComponentById(String id) {
        return componentManager.getGroupComponentById(id);
    }

    public GroupComponent deleteGroupComponent(String id) throws PersistenceException {
        return componentManager.deleteGroupComponent(id);
    }

    public GroupComponent saveGroupComponent(GroupComponent component) throws PersistenceException {
        return componentManager.saveGroupComponent(component);
    }

    public GroupComponent updateGroupComponent(GroupComponent component) throws PersistenceException {
        return componentManager.updateGroupComponent(component);
    }
    //endregion

    //region DataSourceComponent
    public List<DataSourceComponent> getDataSourceComponents() {
        return componentManager.getDataSourceComponents();
    }

    public DataSourceComponent getDataSourceInstance(String id) {
        return componentManager.getDataSourceInstance(id);
    }

    public DataSourceComponent saveDataSourceComponent(DataSourceComponent component) throws PersistenceException {
        return componentManager.saveDataSourceComponent(component);
    }
    //endregion

    //region WorkflowDescriptor
    public List<WorkflowDescriptor> getAllWorkflows() {
        return workflowManager.getAllWorkflows();
    }

    public WorkflowDescriptor getWorkflowDescriptor(long identifier) {
        return workflowManager.getWorkflowDescriptor(identifier);
    }

    public List<WorkflowDescriptor> getUserWorkflowsByStatus(String user, int statusId) {
        return workflowManager.getUserWorkflowsByStatus(user, statusId);
    }

    public List<WorkflowDescriptor> getUserPublishedWorkflowsByVisibility(String user, int visibilityId) {
        return workflowManager.getUserPublishedWorkflowsByVisibility(user, visibilityId);
    }

    public List<WorkflowDescriptor> getOtherPublicWorkflows(String user) {
        return workflowManager.getOtherPublicWorkflows(user);
    }

    public WorkflowDescriptor saveWorkflowDescriptor(WorkflowDescriptor workflow) throws PersistenceException {
        return workflowManager.saveWorkflowDescriptor(workflow);
    }

    public WorkflowDescriptor updateWorkflowDescriptor(WorkflowDescriptor workflow) throws PersistenceException {
        return workflowManager.updateWorkflowDescriptor(workflow);
    }

    public WorkflowDescriptor deleteWorkflowDescriptor(Long workflowId) throws PersistenceException {
        return workflowManager.deleteWorkflowDescriptor(workflowId);
    }
    //endregion

    //region WorkflowNodeDescriptor
    public WorkflowNodeDescriptor getWorkflowNodeById(Long id) {
        return workflowManager.getWorkflowNodeById(id);
    }

    @Transactional
    public List<WorkflowNodeDescriptor> getWorkflowNodesById(Long... ids) {
        return workflowManager.getWorkflowNodesById(ids);
    }

    public List<WorkflowNodeDescriptor> getWorkflowNodesByComponentId(long workflowId, String componentId) {
        return workflowManager.getWorkflowNodesByComponentId(workflowId, componentId);
    }

    public WorkflowNodeDescriptor saveWorkflowNodeDescriptor(WorkflowNodeDescriptor node, WorkflowDescriptor workflow) throws PersistenceException {
        return workflowManager.saveWorkflowNodeDescriptor(node, workflow);
    }

    public WorkflowNodeDescriptor updateWorkflowNodeDescriptor(WorkflowNodeDescriptor node) throws PersistenceException {
        return workflowManager.updateWorkflowNodeDescriptor(node);
    }

    public void delete(WorkflowNodeDescriptor nodeDescriptor) {
        workflowManager.delete(nodeDescriptor);
    }

//endregion

    //region ExecutionJob, ExecutionTask and ExecutionGroup
    public List<ExecutionJob> getAllJobs() {
        return executionManager.getAllJobs();
    }

    public List<ExecutionJob> getJobs(long workflowId) {
        return executionManager.getJobs(workflowId);
    }

    public ExecutionJob getJobById(long jobId) {
        return executionManager.getJobById(jobId);
    }

    public List<ExecutionJob> getJobs(ExecutionStatus status) {
        return executionManager.getJobs(status);
    }

    public List<ExecutionJob> getJobs(String userName, Set<ExecutionStatus> statuses) {
        return executionManager.getJobs(userName, statuses);
    }

    public ExecutionJob saveExecutionJob(ExecutionJob job) throws PersistenceException {
        return executionManager.saveExecutionJob(job);
    }

    public ExecutionJob updateExecutionJob(ExecutionJob job) throws PersistenceException {
        return executionManager.updateExecutionJob(job);
    }

    public List<ExecutionTask> getRunningTasks() {
        return executionManager.getRunningTasks();
    }

    public List<ExecutionTaskSummary> getTasksStatus(long jobId) {
        return executionManager.getTasksStatus(jobId);
    }

    public ExecutionTask getTaskById(Long id) throws PersistenceException {
        return executionManager.getTaskById(id);
    }

    public ExecutionTask getTaskByJobAndNode(long jobId, long nodeId) {
        return executionManager.getTaskByJobAndNode(jobId, nodeId);
    }

    public ExecutionTask getTaskByGroupAndNode(long groupId, long nodeId) {
        return executionManager.getTaskByGroupAndNode(groupId, nodeId);
    }

    public ExecutionTask getTaskByResourceId(String id) throws PersistenceException {
        return executionManager.getTaskByResourceId(id);
    }

    public ExecutionTask saveExecutionTask(ExecutionTask task, ExecutionJob job) throws PersistenceException {
        return executionManager.saveExecutionTask(task, job);
    }

    public ExecutionTask updateExecutionTask(ExecutionTask task) throws PersistenceException {
        return executionManager.updateExecutionTask(task);
    }

    public ExecutionTask updateTaskStatus(ExecutionTask task, ExecutionStatus newStatus) throws PersistenceException {
        return executionManager.updateTaskStatus(task, newStatus);
    }

    public ExecutionTask saveExecutionGroupSubTask(ExecutionTask task, ExecutionGroup taskGroup) throws PersistenceException {
        return executionManager.saveExecutionGroupSubTask(task, taskGroup);
    }

    public ExecutionTask saveExecutionGroupWithSubTasks(ExecutionGroup taskGroup, ExecutionJob job) throws PersistenceException {
        return executionManager.saveExecutionGroupWithSubTasks(taskGroup, job);
    }
    //endregion

    //region Query
    public Query findQueryById(long id) {
        return queryManager.findById(id);
    }

    public Query getQuery(String userId, String sensor, String dataSource, long workflowNodeId) {
        return queryManager.findByUserIdAndSensorAndDataSourceAndWorkflowNodeId(userId, sensor, dataSource, workflowNodeId);
    }

    public List<Query> getQueries(String userId, long nodeId) {
        return queryManager.findByUserIdAndWorkflowNodeId(userId, nodeId);
    }

    public List<Query> getQueries(String userId, String sensor, String dataSource) {
        return queryManager.findByUserIdAndSensorAndDataSource(userId, sensor, dataSource);
    }

    public List<Query> getQueries(String userId) {
        return queryManager.findByUserId(userId);
    }

    public List<Query> getQueriesBySensor(String userId, String sensor) {
        return queryManager.findByUserIdAndSensor(userId, sensor);
    }

    public List<Query> getQueriesByDataSource(String userId, String dataSource) {
        return queryManager.findByUserIdAndDataSource(userId, dataSource);
    }

    public Page<Query> getAllQueries (Pageable pageable) {
        return queryManager.findAll(pageable);
    }

    public Query saveQuery(Query query) throws PersistenceException {
        return queryManager.saveQuery(query);
    }

    public void removeQuery(Query query) {
        queryManager.removeQuery(query);
    }

    //endregion

    //region Message
    public Page<Message> getUserMessages(String user, Integer pageNumber) {
        return notificationManager.getUserMessages(user, pageNumber);
    }

    @Override
    public Message saveMessage(Message message) throws PersistenceException {
        return notificationManager.saveMessage(message);
    }
    //endregion

    // region User
    public User addNewUser(final User newUserInfo) throws PersistenceException {
        return userManager.addNewUser(newUserInfo);
    }

    public List<User> findUsersByStatus(UserStatus userStatus) {
        return userManager.findUsersByStatus(userStatus);
    }

    public Map<String, String[]> getAllUsersUnicityInfo() {
        return userManager.getAllUsersUnicityInfo();
    }

    public User findUserByUsername(final String username) {
        return userManager.findUserByUsername(username);
    }

    public List<UserPreference> getUserPreferences(String userName) throws PersistenceException {
        return userManager.getUserPreferences(userName);
    }

    public String getUserOrganization(String userName) throws PersistenceException {
        return userManager.getUserOrganization(userName);
    }

    public List<Group> getUserGroups(String userName) {
        return userManager.getUserGroups(userName);
    }

    public boolean checkLoginCredentials(String userName, String password) {
        return userManager.checkLoginCredentials(userName, password);
    }

    public User updateUser(User updatedInfo, boolean fromAdmin) throws PersistenceException {
        return userManager.updateUser(updatedInfo, fromAdmin);
    }

    public void updateUserLastLoginDate(Long userId, LocalDateTime lastLoginDate) {
        userManager.updateUserLastLoginDate(userId, lastLoginDate);
    }

    public void activateUser(String userName) throws PersistenceException {
        userManager.activateUser(userName);
    }

    public void resetUserPassword(String userName, String resetKey, String newPassword) throws PersistenceException {
        userManager.resetUserPassword(userName, resetKey, newPassword);
    }

    public void disableUser(String userName) throws PersistenceException {
        userManager.disableUser(userName);
    }

    public void deleteUser(String userName) throws PersistenceException {
        userManager.deleteUser(userName);
    }

    public List<UserPreference> saveOrUpdateUserPreferences(String username, List<UserPreference> newUserPreferences) throws PersistenceException {
        return userManager.saveOrUpdateUserPreferences(username, newUserPreferences);
    }

    public List<UserPreference> removeUserPreferences(String username, List<String> userPrefsKeysToDelete) throws PersistenceException {
        return userManager.removeUserPreferences(username, userPrefsKeysToDelete);
    }

    public List<Group> getGroups() {
        return userManager.getGroups();
    }

// endregion
}
