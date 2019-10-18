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
package ro.cs.tao.persistence.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.annotations.TypeDef;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.persistence.data.jsonutil.JsonStringType;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@PropertySource("classpath:persistence/persistence.properties")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class DatabaseConfiguration implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = Logger.getLogger(DatabaseConfiguration.class.getName());

    /**
     * Constant for the database driver class property name (within .properties
     * file)
     */
    private static final String PROPERTY_NAME_DATABASE_DRIVER = "spring.database.driverClassName";

    /**
     * Constant for the DB connection URL
     */
    private static final String PROPERTY_NAME_DATABASE_URL = "spring.datasource.url";

    /**
     * Constant for the DB connection username
     */
    private static final String PROPERTY_NAME_DATABASE_USERNAME = "spring.datasource.username";

    /**
     * Constant for the DB connection password
     */
    private static final String PROPERTY_NAME_DATABASE_PASSWORD = "spring.datasource.password";

    /**
     * C3p0 Connection Pool minimum pool size
     */
    private static final String PROPERTY_NAME_DATABASE_CONNECTION_MINPOOLSIZE = "spring.datasource.minPoolSize";

    /**
     * C3p0 Connection Pool initial pool size
     */
    private static final String PROPERTY_NAME_DATABASE_CONNECTION_INITIALPOOLSIZE = "spring.datasource.initialPoolSize";

    /**
     * C3p0 Connection Pool maximum pool size
     */
    private static final String PROPERTY_NAME_DATABASE_CONNECTION_MAXPOOLSIZE = "spring.datasource.maxPoolSize";

    /**
     * C3p0 Connection Pool maximum statements
     */
    private static final String PROPERTY_NAME_DATABASE_CONNECTION_MAXSTATEMENTS = "spring.datasource.maxStatements";

    /**
     * C3p0 Connection Pool idle test period
     */
    private static final String PROPERTY_NAME_DATABASE_CONNECTION_IDLETESTPERIOD = "spring.datasource.idleConnectionTestPeriod";

    /**
     * C3p0 Connection Pool login timeout
     */
    //private static final String PROPERTY_NAME_DATABASE_CONNECTION_LOGINTIMEOUT = "spring.datasource.loginTimeout";

    /**
     * C3p0 Connection Pool test connection on checkout
     */
    private static final String PROPERTY_NAME_DATABASE_CONNECTION_TESTONCHECKOUT = "spring.datasource.testConnectionOnCheckout";

    /**
     * Constant for the hibernate dialect property name (within .properties
     * file)
     */
    private static final String PROPERTY_NAME_HIBERNATE_DIALECT = "hibernate.dialect";

    /**
     * Constant for the spring JPA hibernate dialect property name (within .properties
     * file)
     */
    private static final String PROPERTY_NAME_SPRING_JPA_HIBERNATE_DIALECT = "spring.jpa.properties.hibernate.dialect";

    private static final String PROPERTY_NAME_SPRING_JPA_HIBERNATE_LOB_CREATION = "spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation";

    /**
     * Constant for the hibernate format sql flag property name (within
     * .properties file)
     */
    private static final String PROPERTY_NAME_HIBERNATE_FORMAT_SQL = "hibernate.format_sql";

    /**
     * Constant for the hibernate naming strategy property name (within
     * .properties file)
     */
    private static final String PROPERTY_NAME_HIBERNATE_NAMING_STRATEGY = "hibernate.ejb.naming_strategy";

    /**
     * Constant for the hibernate connection handling mode
     * (https://jira.spring.io/browse/SPR-14393)
     */
    private static final String PROPERTY_NAME_HIBERNATE_CONNECTION_HANDLING_MODE = "hibernate.connection.handling_mode";

    /**
     * Constant for the hibernate connection release mode
     */
    private static final String PROPERTY_NAME_HIBERNATE_CONNECTION_RELEASE_MODE = "hibernate.connection.release_mode";

    private static final String PROPERTY_NAME_HIBERNATE_TRANSACTION_AUTO_CLOSE_SESSION = "hibernate.transaction.auto_close_session";

    /**
     * Constant for the hibernate show sql flag property name (within
     * .properties file)
     */
    private static final String PROPERTY_NAME_HIBERNATE_SHOW_SQL = "hibernate.show_sql";

    private static final String PROPERTY_NAME_HIBERNATE_MERGE_ENTITIES = "hibernate.event.merge.entity_copy_observer";

    /**
     * Constant for the Entity Manager packages to scan property name (within
     * .properties file)
     */
    private static final String PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN = "entitymanager.packages.to.scan";

    /**
     * Application environment, used to extract the properties
     */
    @Resource
    private Environment environment;

    private List<ComboPooledDataSource> createdBeans = new ArrayList<>();

    /**
     * Empty constructor
     */
    public DatabaseConfiguration() {
        // empty constructor
    }

    /**
     * Data source with connection pool
     */
    @Bean
    public DataSource dataSource() {
        final ComboPooledDataSource dataSource = new ComboPooledDataSource();
        try {

            dataSource.setDriverClass(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_DRIVER));

            // DB URL + user name + pass from tao.properties
            dataSource.setJdbcUrl(ConfigurationManager.getInstance().getValue(PROPERTY_NAME_DATABASE_URL));

            dataSource.setUser(ConfigurationManager.getInstance().getValue(PROPERTY_NAME_DATABASE_USERNAME));
            dataSource.setPassword(ConfigurationManager.getInstance().getValue(PROPERTY_NAME_DATABASE_PASSWORD));

            dataSource.setInitialPoolSize(Integer
              .parseInt(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_CONNECTION_INITIALPOOLSIZE)));
            dataSource.setMinPoolSize(
              Integer.parseInt(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_CONNECTION_MINPOOLSIZE)));
            dataSource.setMaxPoolSize(
              Integer.parseInt(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_CONNECTION_MAXPOOLSIZE)));
            dataSource.setMaxStatements(
              Integer.parseInt(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_CONNECTION_MAXSTATEMENTS)));
            dataSource.setIdleConnectionTestPeriod(Integer
              .parseInt(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_CONNECTION_IDLETESTPERIOD)));
//			dataSource.setLoginTimeout(
//					Integer.parseInt(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_CONNECTION_LOGINTIMEOUT)));
            dataSource.setTestConnectionOnCheckout(Boolean.getBoolean(environment.getRequiredProperty(PROPERTY_NAME_DATABASE_CONNECTION_TESTONCHECKOUT)));
            Connection connection;
            if ((connection = dataSource.getConnection()) == null) {
                logger.log(Level.SEVERE, "Database connection cannot be established!");
            } else {
                String[] jdbcUrlTokens = dataSource.getJdbcUrl().split(":");
                logger.fine(String.format("Using %s as database driver to connect to '%s'",
                                          dataSource.getDriverClass(), jdbcUrlTokens[2]));
                connection.close();
            }
        } catch (SQLException | IllegalStateException | PropertyVetoException e) {
            logger.log(Level.SEVERE, "Error configuring data source: " + e.getMessage());
            logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(e));
        }
        // add it to the internal list to be cleaned later
        createdBeans.add(dataSource);

        return dataSource;
    }

    /**
     * Transaction Manager retrieval
     *
     * @return transaction manager
     * @throws ClassNotFoundException
     */
    @Bean
    public JpaTransactionManager transactionManager() throws ClassNotFoundException {

        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        final JpaDialect jpaDialect = new HibernateJpaDialect();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        transactionManager.setJpaDialect(jpaDialect);
        return transactionManager;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean
          .setPackagesToScan(environment.getRequiredProperty(PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN));
        entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setPersistenceXmlLocation("classpath:META-INF/persistence.xml");
        entityManagerFactoryBean.setPersistenceUnitName("tao");

        final Properties jpaProperties = new Properties();
        jpaProperties.put(PROPERTY_NAME_HIBERNATE_DIALECT,
          environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_DIALECT));
        jpaProperties.put(PROPERTY_NAME_SPRING_JPA_HIBERNATE_DIALECT,
          environment.getRequiredProperty(PROPERTY_NAME_SPRING_JPA_HIBERNATE_DIALECT));
        jpaProperties.put(PROPERTY_NAME_SPRING_JPA_HIBERNATE_LOB_CREATION,
                          environment.getRequiredProperty(PROPERTY_NAME_SPRING_JPA_HIBERNATE_LOB_CREATION));
        jpaProperties.put(PROPERTY_NAME_HIBERNATE_FORMAT_SQL,
          environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_FORMAT_SQL));
        jpaProperties.put(PROPERTY_NAME_HIBERNATE_NAMING_STRATEGY,
          environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_NAMING_STRATEGY));
        jpaProperties.put(PROPERTY_NAME_HIBERNATE_SHOW_SQL,
          environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_SHOW_SQL));
        jpaProperties.put(PROPERTY_NAME_HIBERNATE_MERGE_ENTITIES,
          environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_MERGE_ENTITIES));
        jpaProperties.put(PROPERTY_NAME_HIBERNATE_CONNECTION_HANDLING_MODE,
          environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_CONNECTION_HANDLING_MODE));
        jpaProperties.put(PROPERTY_NAME_HIBERNATE_CONNECTION_RELEASE_MODE,
          environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_CONNECTION_RELEASE_MODE));
        jpaProperties.put(PROPERTY_NAME_HIBERNATE_TRANSACTION_AUTO_CLOSE_SESSION,
          environment.getRequiredProperty(PROPERTY_NAME_HIBERNATE_TRANSACTION_AUTO_CLOSE_SESSION));
        jpaProperties.put("hibernate.enable_lazy_load_no_trans",
          environment.getRequiredProperty("hibernate.enable_lazy_load_no_trans"));
        entityManagerFactoryBean.setJpaProperties(jpaProperties);
        return entityManagerFactoryBean;
    }

    @Override
    public void onApplicationEvent(final ContextClosedEvent event) {
        for (ComboPooledDataSource dataSource : createdBeans) {
            logger.fine("Closing database connexions ...");
            dataSource.close();
            try {
                DataSources.destroy(dataSource);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage());
                logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(e));
            }
        }
        createdBeans.clear();
    }

}
