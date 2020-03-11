/*
 * Copyright 2017-2018 the original author or authors.
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

package org.springframework.cloud.gcp.pubsub.support;

import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.pubsub.v1.PullRequest;

/**
 * Interface used by the {@link org.springframework.cloud.gcp.pubsub.core.PubSubTemplate} to create
 * supporting objects for consuming messages from Pub/Sub subscriptions.
 *
 * @author João André Martins
 * @author Mike Eltsufin
 * @author Artem Bilan
 * @author Doug Hoard
 * @author Chengyuan Zhao
 */
public interface SubscriberFactory {

	/**
	 * Method to get the project id.
	 * @return the project id
	 * @since 1.1
	 */
	String getProjectId();

	/**
	 * Create a {@link Subscriber} for the specified subscription name and wired it up to
	 * asynchronously deliver messages to the provided {@link MessageReceiver}.
	 * @param subscriptionName the name of the subscription
	 * @param receiver the callback for receiving messages asynchronously
	 * @return the {@link Subscriber} that was created to bind the receiver to the subscription
	 */
	Subscriber createSubscriber(String subscriptionName, MessageReceiver receiver);

	/**
	 * Create a {@link PullRequest} for synchronously pulling a number of messages from
	 * a Google Cloud Pub/Sub subscription.
	 * @param subscriptionName the name of the subscription
	 * @param maxMessages the maximum number of pulled messages,
	 * which can be null or must be a positive number
	 * @param returnImmediately causes the pull request to return immediately even
	 * if subscription doesn't contain enough messages to satisfy {@code maxMessages}
	 * @return the pull request that can be executed using a {@link SubscriberStub}
	 */
	PullRequest createPullRequest(String subscriptionName, Integer maxMessages,
			Boolean returnImmediately);

	/**
	 * Create a {@link SubscriberStub} that is needed to execute {@link PullRequest}s.
	 * @return the {@link SubscriberStub} used for executing {@link PullRequest}s
	 */
	SubscriberStub createSubscriberStub();

}
