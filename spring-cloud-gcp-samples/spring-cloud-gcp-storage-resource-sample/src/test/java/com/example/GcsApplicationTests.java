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

package com.example;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This verifies the sample application for using GCP Storage with Spring Resource abstractions.
 *
 * To run the test, set the gcs-resource-test-bucket property in application.properties to the name
 * of your bucket and run: mvn test -Dit.storage
 *
 * @author Daniel Zou
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { GcsApplication.class })
public class GcsApplicationTests {

	@Autowired
	private Storage storage;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Value("${gcs-resource-test-bucket}")
	private String bucketName;

	@Before
	@After
	public void cleanupCloudStorage() {
		Page<Blob> blobs = storage.list(bucketName);
		for (Blob blob : blobs.iterateAll()) {
			blob.delete();
		}
	}

	@Test
	public void testGcsResourceIsLoaded() {
		BlobId blobId = BlobId.of(bucketName, "my-file.txt");
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
		Blob blob = storage.create(blobInfo, "Good Morning!".getBytes(StandardCharsets.UTF_8));

		Awaitility.await().atMost(15, TimeUnit.SECONDS)
				.untilAsserted(() -> {
					String result = testRestTemplate.getForObject("/", String.class);
					assertThat(result).isEqualTo("Good Morning!\n");
				});

		testRestTemplate.postForObject("/", "Good Night!", String.class);
		Awaitility.await().atMost(15, TimeUnit.SECONDS)
				.untilAsserted(() -> {
					String result = testRestTemplate.getForObject("/", String.class);
					assertThat(result).isEqualTo("Good Night!\n");
				});
	}
}
