/*
 *  Copyright 2018 original author or authors.
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

package org.springframework.cloud.gcp.pubsub.support.converter;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.pubsub.v1.PubsubMessage;

import org.springframework.integration.support.json.JacksonJsonUtils;

/**
 * A message converter using Jackson JSON.
 *
 * @author Chengyuan Zhao
 */
public class JacksonMessageConverter implements PubSubMessageConverter {

	private final ObjectMapper objectMapper;

	/**
	 * Constructor
	 * @param trustedPackages the packages trusted for deserialization in addition to the
	 * default trusted packages listed in {@link JacksonJsonUtils}.
	 */
	public JacksonMessageConverter(String... trustedPackages) {
		this.objectMapper = JacksonJsonUtils.messagingAwareMapper(trustedPackages);
	}

	@Override
	public byte[] toPayload(Object object) throws IOException {
		try {
			return this.objectMapper.writeValueAsBytes(object);
		}
		catch (JsonProcessingException e) {
			throw new IOException("Jackson serialization of object failed: " + object, e);
		}
	}

	@Override
	public <T> T fromMessage(PubsubMessage message, Class<T> payloadType)
			throws IOException {
		return this.objectMapper.readerFor(payloadType)
				.readValue(message.getData().toByteArray());
	}
}
