/*
 * Copyright 2019-2019 the original author or authors.
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

package org.springframework.cloud.gcp.data.datastore.repository.query;

import com.google.cloud.datastore.Cursor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author Dmitry Solomakha
 */
public class CursorPageable extends PageRequest {
	Cursor cursor;

	Long totalCount;

	CursorPageable(Pageable pageable, Cursor cursor, Long totalCount) {
		super(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
		this.cursor = cursor;
		this.totalCount = totalCount;
	}

	public static Pageable from(Pageable pageable, Cursor cursor, Long totalCount) {
		if (pageable.isUnpaged()) {
			return pageable;
		}
		return new CursorPageable(pageable, cursor, totalCount);
	}

	@Override
	public Pageable next() {
		return from(super.next(), this.cursor, this.totalCount);
	}

	public Cursor getCursor() {
		return this.cursor;
	}

	public Long getTotalCount() {
		return this.totalCount;
	}
}
