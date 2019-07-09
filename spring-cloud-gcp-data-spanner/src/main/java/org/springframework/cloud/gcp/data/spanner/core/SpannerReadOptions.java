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

package org.springframework.cloud.gcp.data.spanner.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.cloud.Timestamp;
import com.google.cloud.spanner.Options.ReadOption;
import com.google.cloud.spanner.TimestampBound;

import org.springframework.util.Assert;

/**
 * Encapsulates Cloud Spanner read options.
 *
 * @author Chengyuan Zhao
 * @author Mike Eltsufin
 *
 * @since 1.1
 */
public class SpannerReadOptions implements Serializable {

	private transient List<ReadOption> readOptions = new ArrayList<>();

	private TimestampBound timestampBound;

	private String index;

	private Set<String> includeProperties;

	private boolean allowPartialRead;

	/**
	 * Constructor to create an instance. Use the extension-style add/set functions to add
	 * options and settings.
	 */
	public SpannerReadOptions() {
	}

	public SpannerReadOptions addReadOption(ReadOption readOption) {
		Assert.notNull(readOption, "Valid read option is required!");
		this.readOptions.add(readOption);
		return this;
	}

	public Set<String> getIncludeProperties() {
		return this.includeProperties;
	}

	public SpannerReadOptions setIncludeProperties(Set<String> includeProperties) {
		this.includeProperties = includeProperties;
		return this;
	}

	public TimestampBound getTimestampBound() {
		return this.timestampBound;
	}

	public Timestamp getTimestamp() {
		return this.timestampBound.getMode() == TimestampBound.Mode.READ_TIMESTAMP
				? this.timestampBound.getReadTimestamp()
				: this.timestampBound.getMinReadTimestamp();
	}

	/**
	 * Set if this query should be executed with bounded staleness.
	 * @param timestampBound the timestamp bound. Can be exact or bounded staleness.
	 * @return this options object.
	 */
	public SpannerReadOptions setTimestampBound(TimestampBound timestampBound) {
		this.timestampBound = timestampBound;
		return this;
	}

	public SpannerReadOptions setTimestamp(Timestamp timestamp) {
		this.timestampBound = TimestampBound.ofReadTimestamp(timestamp);
		return this;
	}

	public String getIndex() {
		return this.index;
	}

	public SpannerReadOptions setIndex(String index) {
		this.index = index;
		return this;
	}

	public ReadOption[] getReadOptions() {
		return this.readOptions.toArray(new ReadOption[this.readOptions.size()]);
	}

	public boolean isAllowPartialRead() {
		return this.allowPartialRead;
	}

	public SpannerReadOptions setAllowPartialRead(boolean allowPartialRead) {
		this.allowPartialRead = allowPartialRead;
		return this;
	}
}
