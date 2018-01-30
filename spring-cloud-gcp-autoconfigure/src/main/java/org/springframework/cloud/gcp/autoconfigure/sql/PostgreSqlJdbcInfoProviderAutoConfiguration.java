/*
 *  Copyright 2017 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.gcp.autoconfigure.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the {@link CloudSqlJdbcInfoProvider} for PostgreSQL.
 *
 * @author João André Martins
 */
@Configuration
@ConditionalOnClass({com.google.cloud.sql.postgres.SocketFactory.class, GcpCloudSqlProperties.class,
		org.postgresql.Driver.class})
@ConditionalOnProperty(
		name = "spring.cloud.gcp.sql.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(AppEngineJdbcInfoProviderAutoConfiguration.class)
public class PostgreSqlJdbcInfoProviderAutoConfiguration {

	private static final Log LOGGER =
			LogFactory.getLog(PostgreSqlJdbcInfoProviderAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean(CloudSqlJdbcInfoProvider.class)
	public CloudSqlJdbcInfoProvider defaultPostgreSqlJdbcInfoProvider(
			GcpCloudSqlProperties gcpCloudSqlProperties) {
		CloudSqlJdbcInfoProvider defaultProvider =
				new DefaultCloudSqlJdbcInfoProvider(gcpCloudSqlProperties, DatabaseType.POSTGRESQL);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Default " + DatabaseType.POSTGRESQL.name()
					+ " JdbcUrl provider. Connecting to "
					+ defaultProvider.getJdbcUrl() + " with driver "
					+ defaultProvider.getJdbcDriverClass());
		}

		return defaultProvider;
	}
}
