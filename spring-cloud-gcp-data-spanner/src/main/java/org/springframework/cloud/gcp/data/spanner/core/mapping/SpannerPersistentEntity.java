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

package org.springframework.cloud.gcp.data.spanner.core.mapping;

import java.util.Set;

import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.model.MutablePersistentEntity;

/**
 * Spanner specific interface for a {@link MutablePersistentEntity} stored
 * in a Google Spanner table.
 *
 * @author Ray Tsang
 * @author Chengyuan Zhao
 *
 * @since 1.1
 */
public interface SpannerPersistentEntity<T>
		extends MutablePersistentEntity<T, SpannerPersistentProperty>, ApplicationContextAware {

	/**
	 * Gets the name of the Spanner table.
	 * @return the name of the table.
	 */
	String tableName();

	/**
	 * Gets the column names stored for this entity.
	 * @return the column names.
	 */
	Set<String> columns();

	/**
	 * Gets the primary key properties in order.
	 * @return an array of the properties comprising the primary key in order.
	 */
	SpannerPersistentProperty[] getPrimaryKeyProperties();

	/**
	 * Gets the SpannerMappingContext that can be used to create persistent entities of
	 * types that appear as properties of this entity.
	 * @return
	 */
	SpannerMappingContext getSpannerMappingContext();

	@Override
	SpannerCompositeKeyProperty getIdProperty();
}
