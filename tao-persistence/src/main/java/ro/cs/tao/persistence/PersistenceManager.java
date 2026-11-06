/*
 * Copyright (C) 2018 CS ROMANIA
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

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ro.cs.tao.component.ConditionComponent;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.ogc.WMSComponent;
import ro.cs.tao.component.ogc.WPSComponent;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.persistence.DataSourceConfigurationProvider;
import ro.cs.tao.persistence.managers.*;

import javax.sql.DataSource;

/**
 * DAO
 * Created by Oana H. on 7/18/2017.
 */

@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Scope("singleton")
@Component("persistenceManager")
public class PersistenceManager {

    @Autowired
    private EOProductManager eoProductManager;

    @Autowired
    private VectorDataManager vectorDataManager;

    @Autowired
    private AuxiliaryDataManager auxiliaryDataManager;

    @Autowired
    private NodeFlavorManager nodeFlavorManager;

    @Autowired
    private NodeDBManager nodeManager;

    @Autowired
    private ContainerManager containerManager;

    @Autowired
    private ProcessingComponentManager processingComponentManager;

    @Autowired
    private WPSComponentManager wpsComponentManager;

    @Autowired
    private WMSComponentManager wmsComponentManager;

    @Autowired
    private GroupComponentManager groupComponentManager;

    @Autowired
    private DataSourceComponentManager dataSourceComponentManager;

    @Autowired
    private DataSourceGroupManager dataSourceGroupManager;

    @Autowired
    private DataSourceConnectionManager dataSourceConnectionManager;

    @Autowired
    private DataSourceConfigurationManager dataSourceConfigurationManager;

    private SimpleCache.Cache<String, ProcessingComponent> componentCache;

    @Autowired
    private WorkflowNodeDescriptorManager workflowNodeDescriptorManager;

    @Autowired
    private WorkflowManager workflowManager;

    @Autowired
    private ExecutionJobManager executionJobManager;

    @Autowired
    private ExecutionTaskManager executionTaskManager;

    @Autowired
    private QueryManager queryManager;

    @Autowired
    private NotificationManager notificationManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TagManager tagManager;

    @Autowired
    private NamingRuleManager namingRuleManager;

    @Autowired
    private RepositoryManager repositoryManager;

    @Autowired
    private SiteManager siteManager;

    @Autowired
    private CountryManager countryManager;

    @Autowired
    private AdminL1UnitManager adminL1UnitManager;

    @Autowired
    private AdminL2UnitManager adminL2UnitManager;

    @Autowired
    private AdminKeyManager adminKeyManager;

    @Autowired
    private WebServiceAuthenticationManager wpsAuthenticationManager;

    @Autowired
    private DownloadQueueManager downloadQueueManager;

    @Autowired
    private AuditManager auditManager;

    @Autowired
    private ContainerInstanceManager containerInstanceManager;

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private UserConfigurationManager userConfigurationManager;

    @Autowired
    private VolatileInstanceManager volatileInstanceManager;

    @Autowired
    private ResourceUsageManager resourceUsageManager;

    @Autowired
    private ResourceUsageReportManager resourceUsageReportManager;

    @Autowired
    private WorkflowSubscriptionManager workflowSubscriptionManager;

    @Autowired
    private DataSubscriptionManager dataSubscriptionManager;

    @Autowired
    private ResourceSubscriptionManager resourceSubscriptionManager;

    @Autowired
    private ExternalResourceSubscriptionManager externalResourceSubscriptionManager;

    @Autowired
    private ConditionComponentManager conditionComponentManager;

    @Autowired
    private DryRunMetricsManager dryRunMetricsManager;

    @Autowired
    private QueryTemplateManager queryTemplateManager;

    @Autowired
    private SavedQueryManager savedQueryManager;

    @Autowired
    private ConversationManager conversationManager;

    @Autowired
    private ConversationMessageManager conversationMessageManager;

    @PostConstruct
    public void initialize() {
        componentCache = SimpleCache.create(String.class, ProcessingComponent.class,
                                            id -> processingComponentManager.get(id));
    }

    /**
     * Retrieves the data source associated with the persistence manager.
     *
     * @return The {@link DataSource} instance used for database operations.
     */
    public DataSource getDataSource() { return dataSource; }

    /**
     * Provides access to the raster data functionality.
     *
     * @return An instance of {@link EOProductManager} responsible for handling
     *         raster data-related operations.
     */
    public EOProductManager rasterData() { return eoProductManager; }

    /**
     * Provides access to the manager responsible for handling vector data operations.
     *
     * @return An instance of {@link VectorDataManager} that manages vector data-related functionalities.
     */
    public VectorDataManager vectorData() { return vectorDataManager; }

    /**
     * Provides access to the manager responsible for handling auxiliary data operations.
     *
     * @return An instance of {@link AuxiliaryDataManager} that manages functions related to auxiliary data.
     */
    public AuxiliaryDataManager auxiliaryData() { return auxiliaryDataManager; }

    /**
     * Provides access to the manager responsible for handling node flavor operations.
     *
     * @return An instance of {@link NodeFlavorManager}, which manages functionalities
     *         related to node flavors, such as retrieving available flavors, filtering
     *         specific flavors, and matching flavors based on resource requirements.
     */
    public NodeFlavorManager nodeFlavors() { return nodeFlavorManager; }

    /**
     * Provides access to the manager responsible for handling node configurations and operations.
     *
     * @return An instance of {@link NodeDBManager}, which manages functionalities related to nodes.
     */
    public NodeDBManager nodes() { return nodeManager; }

    /**
     * Provides access to the manager responsible for handling container operations.
     *
     * @return An instance of {@link ContainerManager}, which manages functionalities related to containers,
     *         such as retrieving containers by name, type, and visibility, as well as listing containers
     *         associated with a user.
     */
    public ContainerManager containers() { return containerManager; }

    /**
     * Provides access to the manager responsible for handling processing components.
     *
     * @return An instance of {@link ProcessingComponentManager}, which manages functionalities
     *         related to processing components, such as retrieving, configuring, and managing
     *         processing-related operations within the system.
     */
    public ProcessingComponentManager processingComponents() { return processingComponentManager; }

    /**
     * Provides access to the manager responsible for handling group components.
     *
     * @return An instance of {@link GroupComponentManager}, which manages functionalities
     *         related to group components, such as retrieval, filtering based on
     *         visibility and status, and other operations associated with group
     *         components in the system.
     */
    public GroupComponentManager groupComponents() { return groupComponentManager; }

    /**
     * Provides access to the manager responsible for handling data source components.
     *
     * @return An instance of {@link DataSourceComponentManager}, which manages functionalities
     *         related to data source components, such as retrieval, filtering, and operations
     *         associated with various data source components in the system.
     */
    public DataSourceComponentManager dataSourceComponents() { return dataSourceComponentManager; }

    /**
     * Provides access to the manager responsible for handling data source group functionalities.
     *
     * @return An instance of {@link DataSourceGroupManager}, which manages operations related to
     *         data source component groups, such as retrieval, saving, updating, and processing
     *         of associated components and descriptors.
     */
    public DataSourceGroupManager dataSourceGroups() { return dataSourceGroupManager; }

    /**
     * Provides access to the manager responsible for managing data source connections.
     *
     * @return An instance of {@link DataSourceConnectionManager}, which handles functionalities
     *         related to managing data source connection credentials, retrieving connection
     *         details, and associated operations within the system.
     */
    public DataSourceConnectionManager dataSourceConnectionManager() { return dataSourceConnectionManager; }

    /**
     * Provides access to the manager responsible for handling data source configuration functionalities.
     *
     * @return An instance of {@link DataSourceConfigurationProvider}, which manages operations
     *         related to data source configurations, such as retrieving, saving, updating,
     *         and managing configuration details for data sources.
     */
    public DataSourceConfigurationProvider dataSourceConfigurationProvider() { return dataSourceConfigurationManager; }

    /**
     * Provides access to the cache of processing components.
     *
     * @return An instance of {@link SimpleCache.Cache} where the keys are {@link String}
     *         and the values are {@link ProcessingComponent}, representing the cache
     *         for processing components within the system.
     */
    public SimpleCache.Cache<String, ProcessingComponent> getComponentCache() {
        return componentCache;
    }

    /**
     * Provides access to the manager responsible for handling workflow node descriptor operations.
     *
     * @return An instance of {@link WorkflowNodeDescriptorManager}, which manages functionalities
     *         related to workflow node descriptors, including actions like retrieving, saving,
     *         updating, and managing workflow node data within the system.
     */
    public WorkflowNodeDescriptorManager workflowNodes() { return workflowNodeDescriptorManager; }

    /**
     * Provides access to the manager responsible for workflow-related operations.
     *
     * @return An instance of {@link WorkflowManager}, which manages functionalities
     *         related to workflows, including their creation, execution, modification,
     *         and monitoring.
     */
    public WorkflowManager workflows() { return workflowManager; }

    /**
     * Provides access to the manager responsible for handling execution jobs.
     *
     * @return An instance of {@link ExecutionJobManager}, which manages functionalities
     *         related to execution jobs, including their creation, scheduling, monitoring,
     *         and lifecycle management within the system.
     */
    public ExecutionJobManager jobs() { return executionJobManager; }

    /**
     * Provides access to the manager responsible for handling execution task operations.
     *
     * @return An instance of {@link ExecutionTaskManager}, which manages functionalities
     *         related to execution tasks, including their creation, scheduling, monitoring,
     *         and lifecycle management within the system.
     */
    public ExecutionTaskManager tasks() { return executionTaskManager; }

    /**
     * Provides access to the manager responsible for handling query-related operations.
     *
     * @return An instance of {@link QueryManager}, which manages functionalities
     *         related to queries, such as executing, managing, and retrieving query results.
     */
    public QueryManager queries() { return queryManager; }

    /**
     * Provides access to the manager responsible for handling notification operations.
     *
     * @return An instance of {@link NotificationManager}, which manages functionalities related
     *         to notifications, such as creation, management, and delivery of notifications
     *         within the system.
     */
    public NotificationManager notifications() { return notificationManager; }

    /**
     * Provides access to the manager responsible for user-related operations.
     *
     * @return An instance of {@link UserManager}, which manages functionalities
     *         related to users, such as creation, retrieval, updating, and
     *         management of user profiles and permissions within the system.
     */
    public UserManager users() { return userManager; }

    /**
     * Provides access to the manager responsible for handling naming rules.
     *
     * @return An instance of {@link NamingRuleManager}, which manages functionalities
     *         related to naming conventions, configurations, and validations within the system.
     */
    public NamingRuleManager namingRules() { return namingRuleManager; }

    /**
     * Provides access to the manager responsible for handling tag-related operations.
     *
     * @return An instance of {@link TagManager}, which manages functionalities
     *         related to tags, such as retrieving, validating, and managing tags
     *         within the system.
     */
    public TagManager tags() { return tagManager; }

    /**
     * Provides access to the manager responsible for handling WPS (Web Processing Service)
     * component operations within the system.
     *
     * @return An instance of {@link WPSComponentManager}, which manages functionalities
     *         related to WPS components, such as retrieval, configuration, and execution
     *         of web processing services.
     */
    public WPSComponentManager wpsComponents() { return wpsComponentManager; }

    /**
     * Provides access to the manager responsible for handling WMS (Web Map Service) component operations.
     *
     * @return An instance of {@link WMSComponentManager}, which manages functionalities
     *         related to WMS components, such as retrieval, configuration, and execution
     *         of web mapping services.
     */
    public WMSComponentManager wmsComponents() { return wmsComponentManager; }

    /**
     * Provides access to the repository manager, which handles operations
     * related to managing repositories.
     *
     * @return the repository manager instance
     */
    public RepositoryManager repositories() { return repositoryManager; }

    /**
     * Provides access to the SiteManager instance which manages site-related operations.
     *
     * @return the SiteManager instance for managing site-related functionalities
     */
    public SiteManager sites() { return siteManager; }

    /**
     * Provides access to the manager responsible for country reference data.
     *
     * @return the CountryManager instance
     */
    public CountryManager countries() { return countryManager; }

    /**
     * Provides access to the manager responsible for first-level administrative units.
     *
     * @return the AdminL1UnitManager instance
     */
    public AdminL1UnitManager adminL1Units() { return adminL1UnitManager; }

    /**
     * Provides access to the manager responsible for second-level administrative units.
     *
     * @return the AdminL2UnitManager instance
     */
    public AdminL2UnitManager adminL2Units() { return adminL2UnitManager; }

    /**
     * Provides access to the manager responsible for administrative SSH keys.
     *
     * @return the AdminKeyManager instance
     */
    public AdminKeyManager adminKeys() { return adminKeyManager; }

    /**
     * Provides an instance of the WebServiceAuthenticationManager to handle authentication for the web service.
     *
     * @return an instance of WebServiceAuthenticationManager responsible for managing web service authentication.
     */
    public WebServiceAuthenticationManager webServiceAuthentication() { return wpsAuthenticationManager; }

    /**
     * Retrieves the current instance of the DownloadQueueManager handling the download queue operations.
     *
     * @return the instance of DownloadQueueManager responsible for managing the download queue.
     */
    public DownloadQueueManager downloadQueue() { return downloadQueueManager; }

    /**
     * Provides access to the AuditManager instance, handling application audit functionalities.
     *
     * @return an instance of AuditManager that provides methods for managing audits.
     */
    public AuditManager audit() { return auditManager; }

    /**
     * Provides access to the currently managed container instance.
     *
     * @return the instance of ContainerInstanceManager that manages the container.
     */
    public ContainerInstanceManager containerInstance() { return containerInstanceManager; }

    /**
     * Retrieves the configuration manager instance used for managing application configurations.
     *
     * @return the ConfigurationManager instance responsible for handling configurations.
     */
    public ConfigurationManager configuration() { return configurationManager; }

    /**
     * Retrieves the UserConfigurationManager instance associated with this object.
     *
     * @return the UserConfigurationManager that manages user configurations.
     */
    public UserConfigurationManager userConfiguration() { return userConfigurationManager; }

    /**
     * Provides access to the volatile instance manager.
     *
     * @return the VolatileInstanceManager object that manages volatile instances
     */
    public VolatileInstanceManager volatileInstances() { return volatileInstanceManager; }

    /**
     * Provides access to the current resource usage manager, which is responsible
     * for tracking and managing resource utilization.
     *
     * @return the instance of ResourceUsageManager managing resource usage.
     */
    public ResourceUsageManager resourceUsage() { return resourceUsageManager; }

    /**
     * Provides access to the ResourceUsageReportManager which handles resource usage reporting functionalities.
     *
     * @return an instance of ResourceUsageReportManager responsible for managing resource usage reports.
     */
    public ResourceUsageReportManager resourceUsageReport() { return resourceUsageReportManager; }

    /**
     * Provides access to the WorkflowSubscriptionManager, which manages
     * workflow-related subscriptions and their configurations.
     *
     * @return an instance of WorkflowSubscriptionManager responsible for handling workflow subscriptions.
     */
    public WorkflowSubscriptionManager workflowSubscription() { return workflowSubscriptionManager; }

    /**
     * Provides access to the DataSubscriptionManager instance for managing data subscriptions.
     *
     * @return the DataSubscriptionManager instance used to manage data subscriptions.
     */
    public DataSubscriptionManager dataSubscription() { return dataSubscriptionManager; }

    /**
     * Provides access to the resource subscription manager which handles
     * subscription-related operations within the application.
     *
     * @return an instance of ResourceSubscriptionManager responsible for managing resource subscriptions.
     */
    public ResourceSubscriptionManager resourceSubscription() { return resourceSubscriptionManager; }

    /**
     * Provides access to the ExternalResourceSubscriptionManager instance.
     *
     * @return an instance of ExternalResourceSubscriptionManager responsible for managing external resource subscriptions.
     */
    public ExternalResourceSubscriptionManager externalResourceSubscription() { return externalResourceSubscriptionManager; }

    /**
     * Retrieves the instance of ConditionComponentManager associated with the current context.
     *
     * @return the ConditionComponentManager instance
     */
    public ConditionComponentManager conditionComponents() { return conditionComponentManager; }

    /**
     * Provides access to the DryRunMetricsManager instance to manage dry-run metrics.
     *
     * @return the instance of DryRunMetricsManager
     */
    public DryRunMetricsManager dryRunMetrics() { return dryRunMetricsManager; }

    public QueryTemplateManager queryTemplates() { return queryTemplateManager; }

    public SavedQueryManager savedQueries() { return savedQueryManager; }

    public ConversationManager conversations() { return conversationManager; }

    public ConversationMessageManager conversationMessages() { return conversationMessageManager; }

    /**
     * Retrieves the appropriate manager for the given {@link TaoComponent} instance. The method determines
     * the type of the provided component and returns the corresponding manager instance. If the component
     * type is not recognized, the method returns null.
     *
     * @param <M> The type of the manager that extends {@link TaoComponentManager}.
     * @param component The {@link TaoComponent} instance for which the manager is to be retrieved.
     * @return The manager instance that corresponds to the type of the provided component. Returns null if
     *         the component type is not recognized.
     */
    public <M extends TaoComponentManager> M managerFor(TaoComponent component) {
        if (component instanceof ProcessingComponent) {
            return (M) processingComponents();
        } else if (component instanceof WPSComponent) {
            return (M) wpsComponents();
        } else if (component instanceof WMSComponent) {
            return (M) wmsComponents();
        } else if (component instanceof DataSourceComponent) {
            return (M) dataSourceComponents();
        } else if (component instanceof ConditionComponent) {
            return (M) conditionComponents();
        } else {
            return null;
        }
    }
}

