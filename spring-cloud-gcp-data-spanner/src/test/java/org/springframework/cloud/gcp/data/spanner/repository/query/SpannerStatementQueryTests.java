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

package org.springframework.cloud.gcp.data.spanner.repository.query;

import java.util.ArrayList;
import java.util.List;

import com.google.cloud.spanner.Statement;
import org.junit.Test;

import org.springframework.cloud.gcp.data.spanner.core.SpannerOperations;
import org.springframework.cloud.gcp.data.spanner.core.mapping.SpannerColumn;
import org.springframework.cloud.gcp.data.spanner.core.mapping.SpannerMappingContext;
import org.springframework.cloud.gcp.data.spanner.core.mapping.SpannerTable;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.query.QueryMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Chengyuan Zhao
 */
public class SpannerStatementQueryTests {

	@Test
	public void compoundNameConventionTest() {

		QueryMethod queryMethod = mock(QueryMethod.class);

		when(queryMethod.getName()).thenReturn(
				"findTop3DistinctByActionAndSymbolOrTraderIdAndPriceLessThanOrPriceGreater"
						+ "ThanEqualAndIdIsNotNullAndTraderIdIsNullAndTraderIdLikeAndPriceTrueAndPriceFalse"
						+ "AndPriceGreaterThanAndPriceLessThanEqualOrderByIdDesc");

		SpannerOperations spannerOperations = mock(SpannerOperations.class);

		PartTreeSpannerQuery partTreeSpannerQuery = new PartTreeSpannerQuery(Trade.class,
				queryMethod, spannerOperations, new SpannerMappingContext());

		Object[] params = new Object[]{
		"BUY",
		"abcd",
		"abc123",
		8.88,
		3.33,
		"ignored",
		"ignored",
		"blahblah",
		"ignored",
		"ignored",
		1.11,
		2.22,
		};

		when(spannerOperations.find(any(), (Statement) any(), any()))
				.thenAnswer(invocation -> {
					Statement statement = invocation.getArgument(1);
					assertEquals(
							"SELECT DISTINCT * FROM trades WHERE ( action=@tag0 AND ticker=@tag1 ) OR "
									+ "( trader_id=@tag2 AND price<@tag3 ) OR ( price>=@tag4 AND id<>NULL AND "
									+ "trader_id=NULL AND trader_id LIKE %@tag7 AND price=TRUE AND price=FALSE AND "
									+ "price>@tag10 AND price<=@tag11 )ORDER BY id DESC LIMIT 3; {tag0: BUY, tag1:"
									+ " abcd, tag8: ignored, tag9: ignored, tag10: 1.11, tag6: ignored, tag11: 2.22, "
									+ "tag7: blahblah, tag4: 3.33, tag5: ignored, tag2: abc123, tag3: 8.88}",
							statement.toString());
					return null;
				});

		partTreeSpannerQuery.execute(params);
	}

	@Test(expected = IllegalArgumentException.class)
	public void unspecifiedParametersTest() {

		QueryMethod queryMethod = mock(QueryMethod.class);

		when(queryMethod.getName()).thenReturn(
				"findTop3DistinctIdActionPriceByActionAndSymbolOrTraderIdAndPriceLessThanOrPriceGreater"
						+ "ThanEqualAndIdIsNotNullAndTraderIdIsNullOrderByIdDesc");

		SpannerOperations spannerOperations = mock(SpannerOperations.class);

		PartTreeSpannerQuery partTreeSpannerQuery = new PartTreeSpannerQuery(Trade.class,
				queryMethod, spannerOperations, new SpannerMappingContext());

		Object[] params = new Object[] {
				"BUY",
				"abcd",
				"abc123",
		};

		partTreeSpannerQuery.execute(params);
	}

	@Test(expected = IllegalArgumentException.class)
	public void unsupportedParamTypeTest() {

		QueryMethod queryMethod = mock(QueryMethod.class);

		when(queryMethod.getName()).thenReturn(
				"findTop3DistinctIdActionPriceByActionAndSymbolOrTraderIdAndPriceLessThanOrPriceGreater"
						+ "ThanEqualAndIdIsNotNullAndTraderIdIsNullOrderByIdDesc");

		SpannerOperations spannerOperations = mock(SpannerOperations.class);

		PartTreeSpannerQuery partTreeSpannerQuery = new PartTreeSpannerQuery(Trade.class,
				queryMethod, spannerOperations, new SpannerMappingContext());

		Object[] params = new Object[] {
				"BUY",
				"abcd",
				"abc123",
				8.88,
				3.33,
				new Trade(),
				"ignored",
		};

		partTreeSpannerQuery.execute(params);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void deleteTest() {

		QueryMethod queryMethod = mock(QueryMethod.class);

		when(queryMethod.getName()).thenReturn(
				"deleteTop3DistinctIdActionPriceByActionAndSymbolOrTraderIdAndPriceLessThanOrPriceGreater"
						+ "ThanEqualAndIdIsNotNullAndTraderIdIsNullOrderByIdDesc");

		SpannerOperations spannerOperations = mock(SpannerOperations.class);

		PartTreeSpannerQuery partTreeSpannerQuery = new PartTreeSpannerQuery(Trade.class,
				queryMethod, spannerOperations, new SpannerMappingContext());

		Object[] params = new Object[0];

		partTreeSpannerQuery.execute(params);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void unSupportedPredicateTest() {

		QueryMethod queryMethod = mock(QueryMethod.class);

		when(queryMethod.getName()).thenReturn("countByTraderIdBetween");

		SpannerOperations spannerOperations = mock(SpannerOperations.class);

		PartTreeSpannerQuery partTreeSpannerQuery = new PartTreeSpannerQuery(Trade.class,
				queryMethod, spannerOperations, new SpannerMappingContext());

		Object[] params = new Object[0];

		partTreeSpannerQuery.execute(params);
	}

	@Test
	public void countTest() {

		QueryMethod queryMethod = mock(QueryMethod.class);

		when(queryMethod.getName()).thenReturn("countByAction");

		SpannerOperations spannerOperations = mock(SpannerOperations.class);

		PartTreeSpannerQuery partTreeSpannerQuery = new PartTreeSpannerQuery(Trade.class,
				queryMethod, spannerOperations, new SpannerMappingContext());

		Object[] params = new Object[]{
				"BUY",
		};

		List<Trade> results = new ArrayList<>();
		results.add(new Trade());

		when(spannerOperations.find(any(), (Statement) any(), any()))
				.thenReturn((List) results);

		assertEquals(1, partTreeSpannerQuery.execute(params));
	}

	@Test
	public void existsTest() {

		QueryMethod queryMethod = mock(QueryMethod.class);

		when(queryMethod.getName()).thenReturn("existsByAction");

		SpannerOperations spannerOperations = mock(SpannerOperations.class);

		PartTreeSpannerQuery partTreeSpannerQuery = new PartTreeSpannerQuery(Trade.class,
				queryMethod, spannerOperations, new SpannerMappingContext());

		Object[] params = new Object[]{
				"BUY",
		};

		List<Trade> results = new ArrayList<>();
		results.add(new Trade());

		when(spannerOperations.find(any(), (Statement) any(), any()))
				.thenReturn((List) results);

		assertTrue((boolean) partTreeSpannerQuery.execute(params));
	}

	@Test
	public void notExistsTest() {

		QueryMethod queryMethod = mock(QueryMethod.class);

		when(queryMethod.getName()).thenReturn("existsByAction");

		SpannerOperations spannerOperations = mock(SpannerOperations.class);

		PartTreeSpannerQuery partTreeSpannerQuery = new PartTreeSpannerQuery(Trade.class,
				queryMethod, spannerOperations, new SpannerMappingContext());

		Object[] params = new Object[]{
				"BUY",
		};

		List<Trade> results = new ArrayList<>();

		when(spannerOperations.find(any(), (Statement) any(), any()))
				.thenReturn((List) results);

		assertFalse((boolean) partTreeSpannerQuery.execute(params));
	}

	@SpannerTable(name = "trades")
	private static class Trade {
		@Id
		String id;

		String action;

		Double price;

		Double shares;

		@SpannerColumn(name = "ticker")
		String symbol;

		@SpannerColumn(name = "trader_id")
		String traderId;
	}
}
