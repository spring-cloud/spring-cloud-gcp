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

package org.springframework.cloud.gcp.data.datastore.core;

import java.util.List;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;

import org.springframework.lang.Nullable;

/**
 * An interface of operations that can be done with Cloud Datastore.
 *
 * @author Chengyuan Zhao
 *
 * @since 1.1
 */
public interface DatastoreOperations {

	/**
	 * Get an entity based on a id.
	 * @param id the id of the entity
	 * @param entityClass the type of the entity to get.
	 * @param <T> the class type of the entity.
	 * @return the entity that was found with that id.
	 */
	<T> T findById(Object id, Class<T> entityClass);

}
