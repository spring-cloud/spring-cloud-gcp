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

import java.util.Map;
import java.util.Optional;

import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.Value;
import org.junit.Before;
import org.junit.Test;

import org.springframework.cloud.gcp.data.spanner.core.SpannerOperations;
import org.springframework.cloud.gcp.data.spanner.core.SpannerQueryOptions;
import org.springframework.cloud.gcp.data.spanner.core.mapping.Column;
import org.springframework.cloud.gcp.data.spanner.core.mapping.PrimaryKey;
import org.springframework.cloud.gcp.data.spanner.core.mapping.SpannerDataException;
import org.springframework.cloud.gcp.data.spanner.core.mapping.SpannerMappingContext;
import org.springframework.cloud.gcp.data.spanner.core.mapping.Table;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Chengyuan Zhao
 */
public class SqlSpannerQueryTests {

	private SpannerOperations spannerOperations;

	private QueryMethod queryMethod;

	private EvaluationContextProvider evaluationContextProvider;

	private SpelExpressionParser expressionParser;

	private final Sort sort = Sort.by(Order.asc("COLA"), Order.desc("COLB"));

	private final Pageable pageable = PageRequest.of(3, 10, this.sort);

	@Before
	public void initMocks() {
		this.queryMethod = mock(QueryMethod.class);
		this.spannerOperations = mock(SpannerOperations.class);
		this.expressionParser = new SpelExpressionParser();
	}

	private SqlSpannerQuery createQuery(String sql) {
		return new SqlSpannerQuery(Trade.class, this.queryMethod, this.spannerOperations,
				sql, this.evaluationContextProvider, this.expressionParser,
				new SpannerMappingContext());
	}

	@Test
	public void compoundNameConventionTest() {

		String sql = "SELECT DISTINCT * FROM "
				+ ":org.springframework.cloud.gcp.data.spanner.repository.query.SqlSpannerQueryTests$Trade:"
				+ "@{index=fakeindex}"
				+ " WHERE ( action=@tag0 AND ticker=@tag1 ) OR "
				+ "( trader_id=@tag2 AND price<@tag3 ) OR ( price>=@tag4 AND id<>NULL AND "
				+ "trader_id=NULL AND trader_id LIKE %@tag5 AND price=TRUE AND price=FALSE AND "
				+ "price>@tag6 AND price<=@tag7 )ORDER BY id DESC LIMIT 3;";

		String entityResolvedSql = "SELECT * FROM (SELECT DISTINCT * FROM "
				+ "trades@{index=fakeindex}"
				+ " WHERE ( action=@tag0 AND ticker=@tag1 ) OR "
				+ "( trader_id=@tag2 AND price<@tag3 ) OR ( price>=@tag4 AND id<>NULL AND "
				+ "trader_id=NULL AND trader_id LIKE %@tag5 AND price=TRUE AND price=FALSE AND "
				+ "price>@tag6 AND price<=@tag7 )ORDER BY id DESC LIMIT 3) "
				+ "ORDER BY COLA ASC , COLB DESC LIMIT 10 OFFSET 30";

		Object[] params = new Object[] { "BUY", this.pageable, "abcd", "abc123", 8.88,
				3.33, "blahblah",
				1.11, 2.22, };

		String[] paramNames = new String[] { "tag0", "ignoredPageable", "tag1", "tag2",
				"tag3", "tag4",
				"tag5", "tag6", "tag7" };

		Parameters parameters = mock(Parameters.class);

		when(this.queryMethod.getParameters()).thenReturn(parameters);
		when(parameters.getNumberOfParameters()).thenReturn(paramNames.length);
		when(parameters.getParameter(anyInt())).thenAnswer(invocation -> {
			int index = invocation.getArgument(0);
			Parameter param = mock(Parameter.class);
			when(param.getName()).thenReturn(Optional.of(paramNames[index]));
			when(param.getType()).thenReturn((Class) params[index].getClass());
			return param;
		});

		SqlSpannerQuery sqlSpannerQuery = createQuery(sql);

		when(this.spannerOperations.query(eq(Trade.class), (Statement) any(), any()))
				.thenAnswer(invocation -> {
					Statement statement = invocation.getArgument(1);
					SpannerQueryOptions queryOptions = invocation.getArgument(2);
					assertTrue(queryOptions.isAllowPartialRead());

					assertEquals(entityResolvedSql, statement.getSql());

					Map<String, Value> paramMap = statement.getParameters();

					assertEquals(params[0], paramMap.get("tag0").getString());
					assertEquals(params[2], paramMap.get("tag1").getString());
					assertEquals(params[3], paramMap.get("tag2").getString());
					assertEquals(params[4], paramMap.get("tag3").getFloat64());
					assertEquals(params[5], paramMap.get("tag4").getFloat64());
					assertEquals(params[6], paramMap.get("tag5").getString());
					assertEquals(params[7], paramMap.get("tag6").getFloat64());
					assertEquals(params[8], paramMap.get("tag7").getFloat64());

					return null;
				});

		sqlSpannerQuery.execute(params);

		verify(this.spannerOperations, times(1)).query(any(), (Statement) any(), any());
	}

	@Test(expected = SpannerDataException.class)
	public void multiStatementDisallowedTest() {
		String sql = "SELECT * FROM table; SELECT * FROM table2;";

		Parameters parameters = mock(Parameters.class);
		when(this.queryMethod.getParameters()).thenReturn(parameters);
		when(parameters.getNumberOfParameters()).thenReturn(0);

		SqlSpannerQuery sqlSpannerQuery = createQuery(sql);

		sqlSpannerQuery.execute(new Object[] {});
	}

	@Test(expected = SpannerDataException.class)
	public void multiplePageableSortTest() {
		String sql = "SELECT * FROM table;";

		Parameters parameters = mock(Parameters.class);
		when(this.queryMethod.getParameters()).thenReturn(parameters);
		when(parameters.getNumberOfParameters()).thenReturn(0);

		SqlSpannerQuery sqlSpannerQuery = createQuery(sql);

		sqlSpannerQuery.execute(new Object[] { this.pageable, this.sort });
	}

	@Test(expected = SpannerDataException.class)
	public void mutliplePageableTest() {
		String sql = "SELECT * FROM table;";

		Parameters parameters = mock(Parameters.class);
		when(this.queryMethod.getParameters()).thenReturn(parameters);
		when(parameters.getNumberOfParameters()).thenReturn(0);

		SqlSpannerQuery sqlSpannerQuery = createQuery(sql);

		sqlSpannerQuery.execute(new Object[] { this.pageable, this.pageable });
	}

	@Test(expected = SpannerDataException.class)
	public void mutlipleSortTest() {
		String sql = "SELECT * FROM table;";

		Parameters parameters = mock(Parameters.class);
		when(this.queryMethod.getParameters()).thenReturn(parameters);
		when(parameters.getNumberOfParameters()).thenReturn(0);

		SqlSpannerQuery sqlSpannerQuery = createQuery(sql);

		sqlSpannerQuery.execute(new Object[] { this.sort, this.sort });
	}

	@Table(name = "trades")
	private static class Trade {
		@PrimaryKey
		String id;

		String action;

		Double price;

		Double shares;

		@Column(name = "ticker")
		String symbol;

		@Column(name = "trader_id")
		String traderId;
	}
}
