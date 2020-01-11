/*
 *  Copyright (2020) Subhabrata Ghosh (subho dot ghosh at outlook dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.codekutter.r2db.driver.impl;

import com.codekutter.common.model.IEntity;
import com.codekutter.common.stores.ConnectionException;
import com.codekutter.common.stores.EConnectionState;
import com.codekutter.common.stores.impl.HibernateConnection;
import com.codekutter.common.utils.ConfigUtils;
import com.codekutter.zconfig.common.ConfigurationAnnotationProcessor;
import com.codekutter.zconfig.common.ConfigurationException;
import com.codekutter.zconfig.common.model.EncryptedValue;
import com.codekutter.zconfig.common.model.annotations.ConfigValue;
import com.codekutter.zconfig.common.model.nodes.AbstractConfigNode;
import com.codekutter.zconfig.common.model.nodes.ConfigPathNode;
import com.codekutter.zconfig.common.model.nodes.ConfigValueNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Session;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Getter
@Setter
@Accessors(fluent = true)
public class SearchableHibernateConnection extends HibernateConnection {
    public static final String CONFIG_INDEX_MANAGER = "hibernate.search.default.indexmanager";
    public static final String CONFIG_INDEX_MANAGER_VALUE = "elasticsearch";
    public static final String CONFIG_INDEX_MANAGER_HOSTS = "hibernate.search.default.elasticsearch.host";
    public static final String CONFIG_INDEX_MANAGER_USER = "hibernate.search.default.elasticsearch.username";
    public static final String CONFIG_INDEX_MANAGER_PASSWD = "hibernate.search.default.elasticsearch.password";

    @ConfigValue(name = "hosts")
    private List<String> elasticSearchHosts;
    @ConfigValue(name = "elasticSearchUsername")
    private String elasticSearchUsername;
    @ConfigValue(name = "elasticSearchPassword")
    private EncryptedValue elasticSearchPassword;

    @Override
    public Session connection() throws ConnectionException {
        return super.connection();
    }

    /**
     * Configure this type instance.
     *
     * @param node - Handle to the configuration node.
     * @throws ConfigurationException
     */
    @Override
    @SuppressWarnings("unchecked")
    public void configure(@Nonnull AbstractConfigNode node) throws ConfigurationException {
        Preconditions.checkArgument(node instanceof ConfigPathNode);
        try {
            ConfigurationAnnotationProcessor.readConfigAnnotations(getClass(), (ConfigPathNode) node, this);
            if (!Strings.isNullOrEmpty(hibernateConfig())) {
                File cfg = new File(hibernateConfig());
                if (!cfg.exists()) {
                    throw new ConfigurationException(String.format("Hibernate configuration not found. [path=%s]", cfg.getAbsolutePath()));
                }
                Properties settings = new Properties();
                settings.setProperty(Environment.PASS, dbPassword().getDecryptedValue());
                sessionFactory = new Configuration().configure(cfg).addProperties(settings).buildSessionFactory();
            } else {
                AbstractConfigNode cnode = ConfigUtils.getPathNode(getClass(), (ConfigPathNode) node);
                if (!(cnode instanceof ConfigPathNode)) {
                    throw new ConfigurationException(String.format("Hibernate configuration settings not found. [node=%s]", node.getAbsolutePath()));
                }
                ConfigPathNode cp = (ConfigPathNode) cnode;

                HibernateConfig cfg = new HibernateConfig();
                ConfigurationAnnotationProcessor.readConfigAnnotations(HibernateConfig.class, cp, cfg);

                Configuration configuration = new Configuration();

                Properties settings = new Properties();
                settings.setProperty(Environment.DRIVER, cfg.driver());
                settings.setProperty(Environment.URL, cfg.dbUrl());
                settings.setProperty(Environment.USER, cfg.dbUser());
                settings.setProperty(Environment.PASS, dbPassword().getDecryptedValue());
                settings.setProperty(Environment.DIALECT, cfg.dialect());

                if (cfg.enableCaching()) {
                    if (Strings.isNullOrEmpty(cfg.cacheConfig())) {
                        throw new ConfigurationException("Missing cache configuration file. ");
                    }
                    settings.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "true");
                    settings.setProperty(Environment.CACHE_REGION_FACTORY, HibernateConfig.CACHE_FACTORY_CLASS);
                    if (cfg.enableQueryCaching())
                        settings.setProperty(Environment.USE_QUERY_CACHE, "true");
                    settings.setProperty(HibernateConfig.CACHE_CONFIG_FILE, cfg.cacheConfig());
                }
                if (cp.parmeters() != null) {
                    Map<String, ConfigValueNode> params = cp.parmeters().getKeyValues();
                    if (params != null && !params.isEmpty()) {
                        for (String key : params.keySet()) {
                            settings.setProperty(key, params.get(key).getValue());
                        }
                    }
                }
                settings.setProperty(CONFIG_INDEX_MANAGER, CONFIG_INDEX_MANAGER_VALUE);
                StringBuffer buffer = new StringBuffer();
                for(String host : elasticSearchHosts) {
                    buffer.append(host).append(" ");
                }
                settings.setProperty(CONFIG_INDEX_MANAGER_HOSTS, buffer.toString());
                settings.setProperty(CONFIG_INDEX_MANAGER_USER, elasticSearchUsername);
                settings.setProperty(CONFIG_INDEX_MANAGER_PASSWD, elasticSearchPassword.getDecryptedValue());

                configuration.setProperties(settings);

                if (cfg.classes() != null && !cfg.classes().isEmpty()) {
                    for (String cls : cfg.classes()) {
                        Class<?> c = Class.forName(cls);
                        configuration.addAnnotatedClass(c);
                        addSupportedType((Class<? extends IEntity>) c);
                    }
                }

                ServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(registry);

                state().setState(EConnectionState.Open);
            }
        }  catch (Exception ex) {
            state().setError(ex);
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}