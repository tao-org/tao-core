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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.datasource.persistence.DataSourceConfigurationProvider;
import ro.cs.tao.persistence.managers.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * DAO
 * Created by Oana H. on 7/18/2017.
 */

@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Scope("singleton")
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

    @PostConstruct
    public void initialize() {
        componentCache = SimpleCache.create(String.class, ProcessingComponent.class,
                                            id -> processingComponentManager.get(id));
    }

    public DataSource getDataSource() { return dataSource; }

    public EOProductManager rasterData() { return eoProductManager; }

    public VectorDataManager vectorData() { return vectorDataManager; }

    public AuxiliaryDataManager auxiliaryData() { return auxiliaryDataManager; }

    public NodeFlavorManager nodeFlavors() { return nodeFlavorManager; }

    public NodeDBManager nodes() { return nodeManager; }

    public ContainerManager containers() { return containerManager; }

    public ProcessingComponentManager processingComponents() { return processingComponentManager; }

    public GroupComponentManager groupComponents() { return groupComponentManager; }

    public DataSourceComponentManager dataSourceComponents() { return dataSourceComponentManager; }

    public DataSourceGroupManager dataSourceGroups() { return dataSourceGroupManager; }

    public DataSourceConnectionManager dataSourceConnectionManager() { return dataSourceConnectionManager; }

    public DataSourceConfigurationProvider dataSourceConfigurationProvider() { return dataSourceConfigurationManager; }

    public SimpleCache.Cache<String, ProcessingComponent> getComponentCache() {
        return componentCache;
    }

    public WorkflowNodeDescriptorManager workflowNodes() { return workflowNodeDescriptorManager; }

    public WorkflowManager workflows() { return workflowManager; }

    public ExecutionJobManager jobs() { return executionJobManager; }

    public ExecutionTaskManager tasks() { return executionTaskManager; }

    public QueryManager queries() { return queryManager; }

    public NotificationManager notifications() { return notificationManager; }

    public UserManager users() { return userManager; }

    public NamingRuleManager namingRules() { return namingRuleManager; }

    public TagManager tags() { return tagManager; }

    public WPSComponentManager wpsComponents() { return wpsComponentManager; }

    public WMSComponentManager wmsComponents() { return wmsComponentManager; }

    public RepositoryManager repositories() { return repositoryManager; }

    public SiteManager sites() { return siteManager; }

    public WebServiceAuthenticationManager webServiceAuthentication() { return wpsAuthenticationManager; }

    public DownloadQueueManager downloadQueue() { return downloadQueueManager; }

    public AuditManager audit() { return auditManager; }

    public ContainerInstanceManager containerInstance() { return containerInstanceManager; }

    public ConfigurationManager configuration() { return configurationManager; }

    public VolatileInstanceManager volatileInstances() { return volatileInstanceManager; }

    public ResourceUsageManager resourceUsage() { return resourceUsageManager; }

    public ResourceUsageReportManager resourceUsageReport() { return resourceUsageReportManager; }

    public WorkflowSubscriptionManager workflowSubscription() { return workflowSubscriptionManager; }

    public DataSubscriptionManager dataSubscription() { return dataSubscriptionManager; }

    public ResourceSubscriptionManager resourceSubscription() { return resourceSubscriptionManager; }

    public ExternalResourceSubscriptionManager externalResourceSubscription() { return externalResourceSubscriptionManager; }
}
