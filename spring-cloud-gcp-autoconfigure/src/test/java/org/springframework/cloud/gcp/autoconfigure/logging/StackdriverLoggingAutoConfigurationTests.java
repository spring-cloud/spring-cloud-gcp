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

package org.springframework.cloud.gcp.autoconfigure.logging;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.gcp.logging.CompositeTraceIdExtractor;
import org.springframework.cloud.gcp.logging.TraceIdExtractor;
import org.springframework.cloud.gcp.logging.TraceIdLoggingWebMvcInterceptor;
import org.springframework.cloud.gcp.logging.XCloudTraceIdExtractor;
import org.springframework.cloud.gcp.logging.ZipkinTraceIdExtractor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mike Eltsufin
 * @author João André Martins
 */
public class StackdriverLoggingAutoConfigurationTests {

	private ApplicationContextRunner contextRunner =
			new ApplicationContextRunner()
					.withConfiguration(
							AutoConfigurations.of(StackdriverLoggingAutoConfiguration.class));

	private int countTraceIdInterceptors(Object[] interceptors) {
		int count = 0;
		for (int i = 0; i < interceptors.length; i++) {
			if (interceptors[i] instanceof TraceIdLoggingWebMvcInterceptor) {
				count++;
			}
		}
		return count;
	}

	@Test
	public void testDisabledConfiguration() {
		this.contextRunner.withPropertyValues("spring.cloud.gcp.logging.enabled=false")
				.run(context -> {
					Object[] interceptors = context.getBean(Object[].class);
					assertThat(countTraceIdInterceptors(interceptors)).isEqualTo(0);
				});
	}

	@Test
	public void testRegularConfiguration() {
		this.contextRunner.run(context -> {
			Object[] interceptors = context.getBean(Object[].class);
			assertThat(countTraceIdInterceptors(interceptors)).isEqualTo(1);
		});
	}

	@Test
	public void testGetTraceIdExtractorsDefault() {
		StackdriverLoggingProperties properties = new StackdriverLoggingProperties();
		properties.setTraceIdExtractor(null);
		List<TraceIdExtractor> extractors =
				((CompositeTraceIdExtractor) new StackdriverLoggingAutoConfiguration()
						.traceIdExtractor(properties)).getExtractors();

		assertThat(extractors.size()).isEqualTo(2);
		assertThat(extractors.get(0)).isInstanceOf(XCloudTraceIdExtractor.class);
		assertThat(extractors.get(1)).isInstanceOf(ZipkinTraceIdExtractor.class);
	}

	@Test
	public void testGetTraceIdExtractorsPrioritizeXCloudTrace() {
		StackdriverLoggingProperties properties = new StackdriverLoggingProperties();
		properties.setTraceIdExtractor(TraceIdExtractorType.XCLOUD_ZIPKIN);
		List<TraceIdExtractor> extractors =
				((CompositeTraceIdExtractor) new StackdriverLoggingAutoConfiguration()
						.traceIdExtractor(properties)).getExtractors();

		assertThat(extractors.size()).isEqualTo(2);
		assertThat(extractors.get(0)).isInstanceOf(XCloudTraceIdExtractor.class);
		assertThat(extractors.get(1)).isInstanceOf(ZipkinTraceIdExtractor.class);
	}

	@Test
	public void testGetTraceIdExtractorsPrioritizeZipkinTrace() {
		StackdriverLoggingProperties properties = new StackdriverLoggingProperties();
		properties.setTraceIdExtractor(TraceIdExtractorType.ZIPKIN_XCLOUD);
		List<TraceIdExtractor> extractors =
				((CompositeTraceIdExtractor) new StackdriverLoggingAutoConfiguration()
						.traceIdExtractor(properties)).getExtractors();

		assertThat(extractors.size()).isEqualTo(2);
		assertThat(extractors.get(0)).isInstanceOf(ZipkinTraceIdExtractor.class);
		assertThat(extractors.get(1)).isInstanceOf(XCloudTraceIdExtractor.class);
	}

	@Test
	public void testGetTraceIdExtractorsOnlyXCloud() {
		StackdriverLoggingProperties properties = new StackdriverLoggingProperties();
		properties.setTraceIdExtractor(TraceIdExtractorType.XCLOUD);

		assertThat(new StackdriverLoggingAutoConfiguration().traceIdExtractor(properties))
				.isInstanceOf(XCloudTraceIdExtractor.class);
	}

	@Test
	public void testGetTraceIdExtractorsOnlyZipkin() {
		StackdriverLoggingProperties properties = new StackdriverLoggingProperties();
		properties.setTraceIdExtractor(TraceIdExtractorType.ZIPKIN);

		assertThat(new StackdriverLoggingAutoConfiguration().traceIdExtractor(properties))
				.isInstanceOf(ZipkinTraceIdExtractor.class);
	}
}
