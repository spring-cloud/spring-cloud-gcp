/*
 *  Copyright 2017 original author or authors.
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

package org.springframework.cloud.gcp.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.Resource;

/**
 * Credentials configuration
 *
 * @author João André Martins
 * @author Stéphane Nicoll
 */
public class Credentials {

	/**
	 * Overrides OAuth2 scopes list used by the credentials.
	 *
	 * <p>A list of scopes can be found at https://developers.google.com/identity/protocols/googlescopes.
	 */
	private List<String> scopes = new ArrayList<>();

	/**
	 * Location of the OAuth2 credentials private key.
	 *
	 * <p>Since this is a {@link Resource}, the private key can be in a multitude of locations, such as a local file
	 * system, classpath, URL, etc.
	 */
	private Resource location;

	public Credentials(String... defaultScopes) {
		this.scopes.addAll(Arrays.asList(defaultScopes));
	}

	public List<String> getScopes() {
		return this.scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	public Resource getLocation() {
		return this.location;
	}

	public void setLocation(Resource location) {
		this.location = location;
	}

}
