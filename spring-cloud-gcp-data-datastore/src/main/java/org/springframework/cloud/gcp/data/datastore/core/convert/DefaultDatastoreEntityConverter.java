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

package org.springframework.cloud.gcp.data.datastore.core.convert;

import com.google.cloud.datastore.Entity;

import org.springframework.cloud.gcp.data.datastore.core.mapping.DatastoreDataException;
import org.springframework.cloud.gcp.data.datastore.core.mapping.DatastoreMappingContext;
import org.springframework.cloud.gcp.data.datastore.core.mapping.DatastorePersistentEntity;
import org.springframework.cloud.gcp.data.datastore.core.mapping.DatastorePersistentProperty;
import org.springframework.data.convert.EntityInstantiator;
import org.springframework.data.convert.EntityInstantiators;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.data.mapping.model.PersistentEntityParameterValueProvider;
import org.springframework.util.CollectionUtils;

/**
 * A class for object to entity and entity to object conversions
 *
 * @author Dmitry Solomakha
 *
 * @since 1.1
 */
public class DefaultDatastoreEntityConverter implements DatastoreEntityConverter {
	private DatastoreMappingContext mappingContext;

	private final EntityInstantiators instantiators = new EntityInstantiators();

	private final ReadWriteConversions conversions;


	public DefaultDatastoreEntityConverter(DatastoreMappingContext mappingContext) {
		this(mappingContext, new TwoStepsConversions(new DatastoreCustomConversions()));
	}

	public DefaultDatastoreEntityConverter(DatastoreMappingContext mappingContext,
			ReadWriteConversions conversions) {
		this.mappingContext = mappingContext;
		this.conversions = conversions;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> R read(Class<R> aClass, Entity entity) {
		DatastorePersistentEntity<R> persistentEntity = (DatastorePersistentEntity<R>) this.mappingContext
				.getPersistentEntity(aClass);

		EntityPropertyValueProvider propertyValueProvider = new EntityPropertyValueProvider(entity, this.conversions);

		ParameterValueProvider<DatastorePersistentProperty> parameterValueProvider =
				new PersistentEntityParameterValueProvider<>(persistentEntity, propertyValueProvider, null);

		EntityInstantiator instantiator = this.instantiators.getInstantiatorFor(persistentEntity);
		R instance;
		try {
			instance = instantiator.createInstance(persistentEntity, parameterValueProvider);
			PersistentPropertyAccessor accessor = persistentEntity.getPropertyAccessor(instance);
			persistentEntity.doWithProperties(
					(PropertyHandler<DatastorePersistentProperty>) datastorePersistentProperty -> {
						// if a property is a constructor argument, it was already computed on instantiation
						if (!persistentEntity.isConstructorArgument(datastorePersistentProperty)) {
							Object value = propertyValueProvider.getPropertyValue(datastorePersistentProperty);
							accessor.setProperty(datastorePersistentProperty, value);
						}
					});
		}
		catch (DatastoreDataException e) {
			throw new DatastoreDataException("Unable to read " + persistentEntity.getName() + " entity", e);
		}

		return instance;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void write(Object source, Entity.Builder sink) {
		DatastorePersistentEntity<?> persistentEntity = this.mappingContext.getPersistentEntity(source.getClass());
		PersistentPropertyAccessor accessor = persistentEntity.getPropertyAccessor(source);
		persistentEntity.doWithProperties(
				(DatastorePersistentProperty persistentProperty) -> {
					try {
						Object val = accessor.getProperty(persistentProperty);
						//Check if property is a non-null array
						if (val != null && persistentProperty.isArray() && val.getClass() != byte[].class) {
							//if a propperty is an array, convert it to list
							val = CollectionUtils.arrayToList(val);
						}
						sink.set(persistentProperty.getFieldName(), this.conversions.convertOnWrite(val));
					}
					catch (DatastoreDataException e) {
						throw new DatastoreDataException(
								"Unable to write "
										+ persistentEntity.kindName() + "." + persistentProperty.getFieldName(),
								e);
					}
				});
	}

}
