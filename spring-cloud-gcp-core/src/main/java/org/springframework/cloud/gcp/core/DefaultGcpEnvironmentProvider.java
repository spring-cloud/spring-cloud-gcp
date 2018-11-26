/*
 * Copyright 2018-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gcp.core;


/**
 * Environment-specific implementation determining whether the current GCP environment matches the passed in parameter.
 *
 * <p>Delegates the decision to {@link GcpEnvironment}.
 *
 * @author Elena Felder
 *
 * @since 1.1
 */
public class DefaultGcpEnvironmentProvider implements GcpEnvironmentProvider {

	@Override
	public boolean isCurrentEnvironment(GcpEnvironment candidate) {
		return candidate.matches();
	}
}
