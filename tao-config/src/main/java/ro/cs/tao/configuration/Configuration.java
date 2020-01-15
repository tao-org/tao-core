package ro.cs.tao.configuration;

public final class Configuration {

    public static final class FileSystem {
        public static final String WORKSPACE_LOCATION = "workspace.location";
        public static final String PRODUCTS_LOCATION = "product.location";
        public static final String WEB_SITE_LOCATION = "site.location";
        public static final String MASTER_SHARE = "master.share.name";
        public static final String NODE_SHARE_MOUNT = "node.mount.folder";
    }
    public static final class Services {
        public static final String PORT = "server.port";
        public static final String DATABASE_CONNECTION_STRING = "spring.datasource.url";
        public static final String DATABASE_USER = "spring.datasource.username";
        public static final String DATABASE_PASSWORD = "spring.datasource.password";
    }
    public static final class Messaging {
        public static final String PROVIDER_CLASS = "notification.provider";
    }
    public static final class Topology {
        public static final String PROVIDER_CLASS = "topology.provider";
        public static final String MASTER_USER = "topology.master.user";
        public static final String MASTER_PASSWORD = "topology.master.password";
        public static final String TOOL_CONFIG = "topology.tool_install_config";
        public static final String NODE_POLL_INTERVAL = "topology.node.poll.interval";
    }
    public static final class Docker {
        public static final String PLUGINS_USE_DOCKER = "plugins.use.docker";
        public static final String IMAGES_LOCATION = "tao.docker.images";
        public static final String REGISTRY_URL = "tao.docker.registry";
        public static final String CONTAINER_MOUNT = "tao.docker.bind_mount";
    }
    public static final class DRMAA {
        public static final String SESSION_FACTORY_CLASS = "tao.drmaa.sessionfactory";
        public static final String NATIVE_LIBRARY_PATH = "native.library.path";
        public static final String POLLING_INTERVAL = "tao.drmaa.polling.interval";
        public static final String FORCE_MEMORY_REQUIREMENTS = "tao.force.memory.requirements";
    }
    public static final class Orchestrator {
        public static final String JOB_SELECTOR_CLASS = "job.task.selector";
        public static final String GROUP_SELECTOR_CLASS = "group.task.selector";
    }
    public static final class LDAP {
        public static final String CONTEXT_FACTORY_CLASS = "ldap.context.initial.context.factory";
        public static final String CONTEXT_PROVIDER_URL = "ldap.context.provider.url";
        public static final String AUTHENTICATION_MODE = "ldap.context.security.authentication";
    }
    public static final class Mail {
        public static final String SMTP_AUTHENTICATION_ENABLED = "mail.smtp.auth";
        public static final String SMTP_STARTTLS_ENABLED = "mail.smtp.starttls.enable";
        public static final String SMTP_HOST = "mail.smtp.host";
        public static final String SMTP_PORT = "mail.smtp.port";
        public static final String SMTP_USER = "mail.smtp.username";
        public static final String SMTP_PASSWORD = "mail.smtp.password";
        public static final String SENDER = "mail.from";
        public static final String RECIPIENT = "mail.to";
    }
    public static final class Quota {
        public static final String MANAGER_CLASS = "tao.quota.manager";
    }
    public static final class Scheduling {
        public static final String QUARTZ_SCHEDULER_NAME = "tao.quartz.scheduler.instanceName";
        public static final String QUARTZ_JOB_STORE_DRIVER_CLASS = "tao.quartz.jobStore.driverDelegateClass";
        public static final String QUARTZ_JOB_STORE_TABLE_PREFIX = "tao.quartz.jobStore.tablePrefix";
        public static final String QUARTZ_JOB_STORE_DATASOURCE = "tao.quartz.jobStore.dataSource";
    }
}
