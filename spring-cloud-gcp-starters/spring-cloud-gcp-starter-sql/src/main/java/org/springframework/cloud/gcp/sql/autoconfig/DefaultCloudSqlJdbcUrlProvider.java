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

package org.springframework.cloud.gcp.sql.autoconfig;

import java.io.IOException;

import com.google.api.services.sqladmin.SQLAdmin;

import org.springframework.cloud.gcp.sql.CloudSqlJdbcUrlProvider;
import org.springframework.cloud.gcp.sql.GcpCloudSqlProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Provides default JDBC driver class name and constructs the JDBC URL for Cloud SQL v2
 * when running on local laptop, or in a VM-based environment (e.g., Google Compute
 * Engine, Google Container Engine).
 *
 * @author Ray Tsang
 */
public class DefaultCloudSqlJdbcUrlProvider implements CloudSqlJdbcUrlProvider {
	private final String projectId;

	private final SQLAdmin sqlAdmin;

	private final GcpCloudSqlProperties properties;

	public DefaultCloudSqlJdbcUrlProvider(String projectId, SQLAdmin sqlAdmin, GcpCloudSqlProperties properties) {
		this.projectId = projectId;
		this.sqlAdmin = sqlAdmin;
		this.properties = properties;
		Assert.hasText(projectId,
				"A project ID must be provided.");
		if (StringUtils.isEmpty(properties.getInstanceConnectionName())) {
			Assert.hasText(this.properties.getInstanceName(),
					"Instance Name is required, or specify Instance Connection Name explicitly");
			if (StringUtils.isEmpty(properties.getRegion())) {
				try {
					this.properties.setRegion(determineRegion(this.properties.getInstanceName()));
				}
				catch (IOException e) {
					throw new IllegalArgumentException(
							"Unable to determine Cloud SQL region. Specify the region explicitly, " +
									"or specify Instance Connection Name explicitly ",
							e);
				}
			}
			properties.setInstanceConnectionName(
					String.format("%s:%s:%s", projectId, this.properties.getRegion(),
							this.properties.getInstanceName()));
		}
	}

	protected String determineRegion(String instanceName) throws IOException {
		return this.sqlAdmin.instances().get(this.projectId,
				instanceName).execute().getRegion();
	}

	@Override
	public String getJdbcDriverClass() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	public String getJdbcUrl() {
		return String.format("jdbc:mysql://google/%s?cloudSqlInstance=%s&"
				+ "socketFactory=com.google.cloud.sql.mysql.SocketFactory",
				this.properties.getDatabaseName(),
				this.properties.getInstanceConnectionName());
	}
}
