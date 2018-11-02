/*
 *  Copyright 2017-2018 original author or authors.
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

package org.springframework.cloud.gcp.stream.binder.pubsub.provisioning;

import java.util.UUID;

import org.springframework.cloud.gcp.pubsub.PubSubAdmin;
import org.springframework.cloud.gcp.stream.binder.pubsub.properties.PubSubConsumerProperties;
import org.springframework.cloud.gcp.stream.binder.pubsub.properties.PubSubProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.util.StringUtils;

/**
 * @author João André Martins
 * @author Mike Eltsufin
 */
public class PubSubChannelProvisioner
		implements ProvisioningProvider<ExtendedConsumerProperties<PubSubConsumerProperties>,
		ExtendedProducerProperties<PubSubProducerProperties>> {

	private final PubSubAdmin pubSubAdmin;

	public PubSubChannelProvisioner(PubSubAdmin pubSubAdmin) {
		this.pubSubAdmin = pubSubAdmin;
	}

	@Override
	public ProducerDestination provisionProducerDestination(String name,
			ExtendedProducerProperties<PubSubProducerProperties> properties)
			throws ProvisioningException {
		if (this.pubSubAdmin.getTopic(name) == null) {
			this.pubSubAdmin.createTopic(name);
		}

		return new PubSubProducerDestination(name);
	}

	@Override
	public ConsumerDestination provisionConsumerDestination(String name, String group,
			ExtendedConsumerProperties<PubSubConsumerProperties> properties)
			throws ProvisioningException {
		// Use group name as subscription name
		// Generate anonymous random group, if one not provided
		String subscription = !StringUtils.hasText(group) ? "anonymous." + UUID.randomUUID().toString() : group;

		if (this.pubSubAdmin.getSubscription(subscription) == null) {
			if (properties.getExtension().isAutoCreateResources()) {
				if (this.pubSubAdmin.getTopic(name) == null) {
					this.pubSubAdmin.createTopic(name);
				}

				this.pubSubAdmin.createSubscription(subscription, name);
			}
			else {
				throw new ProvisioningException("Non-existing '" + subscription + "' subscription.");
			}
		}
		return new PubSubConsumerDestination(subscription);
	}
}
