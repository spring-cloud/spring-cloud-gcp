/*
 * Copyright 2019-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.springframework.cloud.gcp.data.firestore;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import com.google.cloud.firestore.PublicClassMapper;
import com.google.firestore.v1.CreateDocumentRequest;
import com.google.firestore.v1.DeleteDocumentRequest;
import com.google.firestore.v1.Document;
import com.google.firestore.v1.FirestoreGrpc.FirestoreStub;
import com.google.firestore.v1.RunQueryRequest;
import com.google.firestore.v1.RunQueryResponse;
import com.google.firestore.v1.StructuredQuery;
import com.google.firestore.v1.Value;
import com.google.firestore.v1.Write;
import com.google.firestore.v1.WriteRequest;
import com.google.firestore.v1.WriteResponse;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gcp.data.firestore.mapping.FirestoreMappingContext;
import org.springframework.cloud.gcp.data.firestore.mapping.FirestorePersistentEntity;
import org.springframework.cloud.gcp.data.firestore.mapping.FirestorePersistentProperty;
import org.springframework.cloud.gcp.data.firestore.util.ObservableReactiveUtil;

/**
 * An implementation of {@link FirestoreReactiveOperations}.
 *
 * @author Dmitry Solomakha
 * @author Chengyuan Zhao
 * @since 1.2
 */
public class FirestoreTemplate implements FirestoreReactiveOperations {

	private final FirestoreStub firestore;

	private final String parent;

	private final String databasePath;

	private final FirestoreMappingContext mappingContext = new FirestoreMappingContext();

	/**
	 * Constructor for FirestoreTemplate.
	 * @param firestore Firestore gRPC stub
	 * @param parent the parent resource. For example:
	 *     projects/{project_id}/databases/{database_id}/documents or
	 *     projects/{project_id}/databases/{database_id}/documents/chatrooms/{chatroom_id}
	 */
	public FirestoreTemplate(FirestoreStub firestore, String parent) {
		this.firestore = firestore;
		this.parent = parent;
		this.databasePath = parent.substring(0, StringUtils.ordinalIndexOf(parent, "/", 4));
	}

	public <T> Mono<T> save(T entity) {
		return Mono.defer(() -> {
			FirestorePersistentEntity<?> persistentEntity = this.mappingContext.getPersistentEntity(entity.getClass());
			FirestorePersistentProperty idProperty = persistentEntity.getIdPropertyOrFail();
			Object idVal = persistentEntity.getPropertyAccessor(entity).getProperty(idProperty);

			Map<String, Value> valuesMap = PublicClassMapper.convertToFirestoreTypes(entity);

			CreateDocumentRequest createDocumentRequest = CreateDocumentRequest.newBuilder()
					.setParent(this.parent)
					.setCollectionId(persistentEntity.collectionName())
					.setDocumentId(idVal.toString())
					.setDocument(Document.newBuilder().putAllFields(valuesMap))
					.build();
			return ObservableReactiveUtil.<Document>unaryCall(
					obs -> this.firestore.createDocument(createDocumentRequest, obs)).then(Mono.just(entity));
		});
	}

	@Override
	public <T> Flux<T> saveAll(Publisher<T> instances) {
		return Flux.defer(() -> {
			Flux<T> input = Flux.from(instances);

			WriteRequest openStreamRequest = WriteRequest.newBuilder().setDatabase(this.databasePath).build();

			AtomicReference<StreamObserver<WriteRequest>> writeRequestObserver = new AtomicReference<>();

			CountDownLatch latch = new CountDownLatch(1);

			Mono<WriteResponse> writeResponses = ObservableReactiveUtil
					.unaryCall(
							(StreamObserver<WriteResponse> obs) -> {
								writeRequestObserver.set(this.firestore.write(obs));
								writeRequestObserver.get().onNext(openStreamRequest);
							}).cache();

			Flux<T> writeFlux = input.flatMap((T entity) -> writeResponses.flatMap((WriteResponse streamIds) -> {
				FirestorePersistentEntity<?> persistentEntity = this.mappingContext
						.getPersistentEntity(entity.getClass());
				FirestorePersistentProperty idProperty = persistentEntity.getIdPropertyOrFail();
				Object idVal = persistentEntity.getPropertyAccessor(entity).getProperty(idProperty);

				Map<String, Value> valuesMap = PublicClassMapper.convertToFirestoreTypes(entity);

				return Mono.fromRunnable(() -> writeRequestObserver.get()
						.onNext(WriteRequest.newBuilder()
								.setStreamId(streamIds.getStreamId())
								.setStreamToken(streamIds.getStreamToken())
								.addWrites(Write.newBuilder()
										.setUpdate(Document.newBuilder()
												.putAllFields(valuesMap)
												.setName(this.parent + "/"
														+ persistentEntity.collectionName() + "/"
														+ idVal.toString())
												.build())
										.build())
								.build()))
						.then(Mono.just(entity));
			})).doOnComplete(latch::countDown);

			// This custom flux is created to specify the doFinally
			return Flux.create(sink -> sink.onRequest(req -> {
				writeFlux.doOnNext(sink::next).doFinally(signalType -> {
					try {
						latch.await();
					 } catch (InterruptedException e) {
					 	throw new FirestoreDataException("Streaming saveAll could not complete.", e);
					 }
					sink.complete();
					writeRequestObserver.get().onCompleted();
				}).subscribe();
			}));
		});
	}

	public <T> Flux<T> findAll(Class<T> clazz) {
		return Flux.defer(() ->
				findAllDocuments(clazz)
						.map(document -> PublicClassMapper.convertToCustomClass(document, clazz)));
	}


	public <T> Mono<Long> deleteAll(Class<T> clazz) {
		return Mono.defer(() ->
			findAllDocuments(clazz).flatMap(this::callDelete).count());
	}

	private Mono<Empty> callDelete(Document doc) {
		DeleteDocumentRequest deleteDocumentRequest = DeleteDocumentRequest.newBuilder().setName(doc.getName())
				.build();
		return ObservableReactiveUtil.unaryCall(
						obs -> this.firestore.deleteDocument(deleteDocumentRequest, obs));
	}

	private <T> Flux<Document> findAllDocuments(Class<T> clazz) {
		FirestorePersistentEntity<?> persistentEntity = this.mappingContext.getPersistentEntity(clazz);
		StructuredQuery structuredQuery = StructuredQuery.newBuilder()
				.addFrom(
						StructuredQuery.CollectionSelector.newBuilder()
								.setCollectionId(persistentEntity.collectionName()).build())
				.build();
		RunQueryRequest request = RunQueryRequest.newBuilder()
				.setParent(this.parent)
				.setStructuredQuery(structuredQuery)
				.build();

		return ObservableReactiveUtil.<RunQueryResponse>streamingCall(obs -> this.firestore.runQuery(request, obs))
				.filter(RunQueryResponse::hasDocument).map(RunQueryResponse::getDocument);
	}
}
