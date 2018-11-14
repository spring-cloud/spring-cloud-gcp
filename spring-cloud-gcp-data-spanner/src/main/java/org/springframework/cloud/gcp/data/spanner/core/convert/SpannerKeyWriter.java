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

package org.springframework.cloud.gcp.data.spanner.core.convert;

import com.google.cloud.spanner.Key;

/**
 * An interface that can create Cloud Spanner keys and utlizes conversion.
 *
 * @author Chengyuan Zhao
 *
 * @since 1.1
 */
public interface SpannerKeyWriter {

	/**
	 * Writes a given object to a Cloud Spanner key.
	 * @param key the object containing the key values. This can already be a Cloud Spanner
	 *     key, a single key component, or an array of key components.
	 * @return the Cloud Spanner key.
	 */
	Key writeToKey(Object key);

	/**
	 * Get the SpannerWriteConverter used to convert types into Cloud Spanner compatible
	 * types.
	 * @return a SpannerWriteConverter
	 */
	SpannerWriteConverter getSpannerWriteConverter();
}
