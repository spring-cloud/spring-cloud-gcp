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

package org.springframework.cloud.gcp.pubsub.integration;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

public class PubSubHeaderMapper implements HeaderMapper<Map<String, String>> {

	/**
	 * Header patterns to map in {@link #fromHeaders(MessageHeaders, Map)}.
	 */
	private String[] outboundHeaderPatternsToMap = {"*"};

	/**
	 * Header patterns to map in {@link #toHeaders(Map)}.
	 */
	private String[] inboundHeaderPatternsToMap = {"*"};

	/**
	 * Set the patterns of the headers to be mapped in {@link #fromHeaders(MessageHeaders, Map)}.
	 * @param outboundHeaderPatternsToMap
	 */
	public void setOutboundHeaderPatternsToMap(String... outboundHeaderPatternsToMap) {
		Assert.notNull(outboundHeaderPatternsToMap, "Header patterns can't be null.");
		Assert.noNullElements(outboundHeaderPatternsToMap, "No header pattern can be null.");
		this.outboundHeaderPatternsToMap =
				Arrays.copyOf(outboundHeaderPatternsToMap, outboundHeaderPatternsToMap.length);
	}

	/**
	 * Set the patterns of the headers to be mapped in {@link #toHeaders(Map)}.
	 * @param inboundHeaderPatternsToMap
	 */
	public void setInboundHeaderPatternsToMap(String[] inboundHeaderPatternsToMap) {
		Assert.notNull(inboundHeaderPatternsToMap, "Header patterns can't be null.");
		Assert.noNullElements(inboundHeaderPatternsToMap, "No header pattern can be null.");
		this.inboundHeaderPatternsToMap = inboundHeaderPatternsToMap;
	}

	/**
	 * Generate headers in {@link com.google.pubsub.v1.PubsubMessage} format from
	 * {@link MessageHeaders}. All headers are converted into strings.
	 *
	 * <p>Will map only the headers that match the patterns in {@code outboundHeaderPatternsMap}.
	 * @param messageHeaders headers to map from
	 * @param pubsubMessageHeaders headers in their final format
	 */
	@Override
	public void fromHeaders(MessageHeaders messageHeaders,
			final Map<String, String> pubsubMessageHeaders) {
		messageHeaders.entrySet().stream()
				.filter(entry -> this.outboundHeaderPatternsToMap != null
						? PatternMatchUtils.simpleMatch(
								this.outboundHeaderPatternsToMap, entry.getKey())
						: false)
				.forEach(entry -> pubsubMessageHeaders.put(
						entry.getKey(), entry.getValue().toString()));
	}

	/**
	 * Generate headers in {@link org.springframework.messaging.Message} format from
	 * {@code Map<String, String>}.
	 *
	 * <p>Will map only the headers that match the patterns in {@code inboundHeaderPatternsMap}.
	 * @param pubsubMessageHeaders headers in {@link com.google.pubsub.v1.PubsubMessage} format
	 * @return a map with headers in the {@link org.springframework.messaging.Message} format
	 */
	@Override
	public Map<String, Object> toHeaders(Map<String, String> pubsubMessageHeaders) {
		return pubsubMessageHeaders.entrySet().stream()
				.filter(entry -> this.inboundHeaderPatternsToMap != null
						? PatternMatchUtils.simpleMatch(
								this.inboundHeaderPatternsToMap, entry.getKey())
						: false)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
