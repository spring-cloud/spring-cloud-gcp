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

package org.springframework.cloud.gcp.data.spanner.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.google.cloud.spanner.Options.ReadOption;
import org.junit.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Chengyuan Zhao
 */
public class SpannerReadOptionsTests {

	@Test(expected = IllegalArgumentException.class)
	public void addNullReadOptionTest() {
		new SpannerReadOptions().addReadOption(null);
	}

	@Test
	public void addReadOptionTest() {
		SpannerReadOptions spannerReadOptions = new SpannerReadOptions();
		ReadOption r1 = mock(ReadOption.class);
		ReadOption r2 = mock(ReadOption.class);
		spannerReadOptions.addReadOption(r1).addReadOption(r2);
		assertThat(Arrays.asList(spannerReadOptions.getReadOptions()),
				containsInAnyOrder(r1, r2));
	}

	@Test
	public void includePropertiesTest() {
		SpannerReadOptions spannerReadOptions = new SpannerReadOptions();
		Set<String> includeProperties = Collections.emptySet();
		assertNull(spannerReadOptions.getIncludeProperties());
		spannerReadOptions.setIncludeProperties(includeProperties);
		assertNotNull(spannerReadOptions.getIncludeProperties());
	}

}
