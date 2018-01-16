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

package org.springframework.cloud.gcp.logging;

import com.google.common.collect.ImmutableList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * MVC Adapter that adds the {@link TraceIdLoggingWebMvcInterceptor}
 *
 * @author Mike Eltsufin
 * @author Chengyuan Zhao
 */
@Configuration
public class LoggingWebMvcConfigurer extends WebMvcConfigurerAdapter {

	private final TraceIdLoggingWebMvcInterceptor interceptor;

	public LoggingWebMvcConfigurer(
			@Autowired(required = false) TraceIdLoggingWebMvcInterceptor interceptor) {
		if (interceptor != null) {
			this.interceptor = interceptor;
		}
		else {
			this.interceptor = new TraceIdLoggingWebMvcInterceptor(
					new CompositeTraceIdExtractor(ImmutableList.of(
							new XCloudTraceIdExtractor(), new ZipkinTraceIdExtractor())));
		}
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this.interceptor);
	}
}
