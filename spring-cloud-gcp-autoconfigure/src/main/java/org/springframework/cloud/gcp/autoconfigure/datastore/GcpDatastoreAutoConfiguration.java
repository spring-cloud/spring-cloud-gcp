/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gcp.autoconfigure.datastore;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.cloud.NoCredentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.DatastoreReaderWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gcp.autoconfigure.core.GcpContextAutoConfiguration;
import org.springframework.cloud.gcp.core.DefaultCredentialsProvider;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.core.UserAgentHeaderProvider;
import org.springframework.cloud.gcp.data.datastore.core.DatastoreOperations;
import org.springframework.cloud.gcp.data.datastore.core.DatastoreTemplate;
import org.springframework.cloud.gcp.data.datastore.core.convert.DatastoreCustomConversions;
import org.springframework.cloud.gcp.data.datastore.core.convert.DatastoreEntityConverter;
import org.springframework.cloud.gcp.data.datastore.core.convert.DatastoreServiceObjectToKeyFactory;
import org.springframework.cloud.gcp.data.datastore.core.convert.DefaultDatastoreEntityConverter;
import org.springframework.cloud.gcp.data.datastore.core.convert.ObjectToKeyFactory;
import org.springframework.cloud.gcp.data.datastore.core.convert.ReadWriteConversions;
import org.springframework.cloud.gcp.data.datastore.core.convert.TwoStepsConversions;
import org.springframework.cloud.gcp.data.datastore.core.mapping.DatastoreMappingContext;
import org.springframework.cloud.gcp.data.datastore.core.util.CachingDatastoreProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides Spring Data classes to use with Cloud Datastore.
 *
 * @author Chengyuan Zhao
 *
 * @since 1.1
 */
@Configuration
@AutoConfigureAfter(GcpContextAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.gcp.datastore.enabled", matchIfMissing = true)
@ConditionalOnClass({ DatastoreOperations.class, Datastore.class })
@EnableConfigurationProperties(GcpDatastoreProperties.class)
public class GcpDatastoreAutoConfiguration {

	private static final Log LOGGER = LogFactory.getLog(GcpDatastoreEmulatorAutoConfiguration.class);

	private final String projectId;

	private final String namespace;

	private final Credentials credentials;

	private final String host;

	GcpDatastoreAutoConfiguration(GcpDatastoreProperties gcpDatastoreProperties,
			GcpProjectIdProvider projectIdProvider,
			CredentialsProvider credentialsProvider) throws IOException {

		this.projectId = (gcpDatastoreProperties.getProjectId() != null)
				? gcpDatastoreProperties.getProjectId()
				: projectIdProvider.getProjectId();
		this.namespace = gcpDatastoreProperties.getNamespace();

		String hostToConnect = gcpDatastoreProperties.getHost();
		if (gcpDatastoreProperties.getEmulator().isEnabled()) {
			hostToConnect = "localhost:" + gcpDatastoreProperties.getEmulator().getPort();
			LOGGER.info("Connecting to a local datastore emulator.");
		}

		if (hostToConnect == null) {
			this.credentials = (gcpDatastoreProperties.getCredentials().hasKey()
					? new DefaultCredentialsProvider(gcpDatastoreProperties)
					: credentialsProvider).getCredentials();
		}
		else {
			// Use empty credentials with Datastore Emulator.
			this.credentials = NoCredentials.getInstance();
		}

		this.host = hostToConnect;
	}

	@Bean
	@ConditionalOnMissingBean
	public DatastoreNamespaceProvider namespaceProvider() {
		return () -> this.namespace;
	}

	@Bean
	@ConditionalOnMissingBean(value = Datastore.class, parameterizedContainer = Supplier.class)
	public Supplier<Datastore> datastoreSupplier(DatastoreNamespaceProvider namespaceProvider,
			Optional<Datastore> datastore) {
		if (datastore.isPresent()) {
			return () -> datastore.get();
		}
		return new CachingDatastoreProvider<>(namespaceProvider, namespace -> {
			DatastoreOptions.Builder builder = DatastoreOptions.newBuilder()
					.setProjectId(this.projectId)
					.setHeaderProvider(new UserAgentHeaderProvider(this.getClass()))
					.setCredentials(this.credentials);
			if (namespace != null) {
				builder.setNamespace(namespace);
			}

			if (this.host != null) {
				builder.setHost(this.host);
			}
			return builder.build().getService();
		});
	}

	@Bean
	@ConditionalOnMissingBean
	public DatastoreCustomConversions datastoreCustomConversions() {
		return new DatastoreCustomConversions();
	}

	@Bean
	@ConditionalOnMissingBean
	public ReadWriteConversions datastoreReadWriteConversions(DatastoreCustomConversions customConversions,
			ObjectToKeyFactory objectToKeyFactory, DatastoreMappingContext datastoreMappingContext) {
		return new TwoStepsConversions(customConversions, objectToKeyFactory, datastoreMappingContext);
	}

	@Bean
	@ConditionalOnMissingBean
	public DatastoreMappingContext datastoreMappingContext() {
		return new DatastoreMappingContext();
	}

	@Bean
	@ConditionalOnMissingBean
	public ObjectToKeyFactory objectToKeyFactory(Supplier<Datastore> datastore) {
		return new DatastoreServiceObjectToKeyFactory(datastore);
	}

	@Bean
	@ConditionalOnMissingBean
	public DatastoreEntityConverter datastoreEntityConverter(DatastoreMappingContext datastoreMappingContext,
			ReadWriteConversions conversions) {
		return new DefaultDatastoreEntityConverter(datastoreMappingContext, conversions);
	}

	@Bean
	@ConditionalOnMissingBean
	public DatastoreTemplate datastoreTemplate(Supplier<? extends DatastoreReaderWriter> datastore,
			DatastoreMappingContext datastoreMappingContext,
			DatastoreEntityConverter datastoreEntityConverter, ObjectToKeyFactory objectToKeyFactory) {
		return new DatastoreTemplate(datastore, datastoreEntityConverter, datastoreMappingContext, objectToKeyFactory);
	}

	/**
	 * This interface is the return type for the bean that provides namespaces.
	 */
	public interface DatastoreNamespaceProvider extends Supplier<String> {

	}
}
