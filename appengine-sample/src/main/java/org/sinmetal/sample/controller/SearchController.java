package org.sinmetal.sample.controller;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.servlet.http.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.sinmetal.sample.util.*;
import org.slim3.controller.*;

import com.google.appengine.api.search.*;
import com.google.appengine.api.search.checkers.*;

/**
 * Search APIに関するサンプル
 * 
 * @see https://cloud.google.com/appengine/docs/java/search/
 * 
 * @author sinmetal
 *
 */
public class SearchController extends SimpleController {

	static final Logger logger = Logger.getLogger(SearchController.class
			.getSimpleName());

	@Override
	protected Navigation run() throws Exception {
		if ("GET".equals(request.getMethod().toUpperCase())) {
			return doGet();
		} else if ("POST".equals(request.getMethod().toUpperCase())) {
			return doPost();
		} else {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return null;
		}
	}

	Navigation doGet() throws JsonGenerationException, JsonMappingException,
			IOException {
		Index index = getIndex();

		String value = request.getParameter("value");

		SearchResultList<ScoredDocument> results = search("hoge:" + value,
				null, true, 100, null, index);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		new ObjectMapper().writeValue(response.getOutputStream(), results);
		return null;
	}

	Navigation doPost() throws JsonGenerationException, JsonMappingException,
			IOException {
		Index index = getIndex();
		// @formatter:off
		Document.Builder builder = Document.newBuilder()
				.setId(UUID.randomUUID().toString()).setLocale(Locale.JAPAN);
		// @formatter:on

		builder.addField(Field.newBuilder().setName("hoge").setText("text"));
		PutResponse res = index.put(builder.build());

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		new ObjectMapper().writeValue(response.getOutputStream(), res.getIds());
		return null;
	}

	SearchResultList<ScoredDocument> search(String queryText,
			SortOptions sortOptions, boolean idsOnly, int limit,
			Cursor paramCursor, Index index) {
		logger.info("queryText : " + queryText);

		try {
			QueryChecker.checkQuery(queryText);
		} catch (IllegalArgumentException e) {
			logger.fine("queryText is too long.");
			throw e;
		}

		QueryOptions.Builder builder = QueryOptions.newBuilder();
		if (paramCursor == null) {
			builder.setCursor(Cursor.newBuilder().setPerResult(true).build());
		} else {
			builder.setCursor(paramCursor);
		}

		if (sortOptions != null) {
			builder.setSortOptions(sortOptions);
		}

		if (limit > 0) {
			builder.setLimit(limit);
		}

		QueryOptions options = builder.setReturningIdsOnly(idsOnly).build();
		Query query = Query.newBuilder().setOptions(options).build(queryText);

		Results<ScoredDocument> searchResutls = null;
		try {
			searchResutls = index.search(query);
		} catch (SearchException searchException) {
			OperationResult operationResult = searchException
					.getOperationResult();
			try {
				logger.info("search exception results : "
						+ new ObjectMapper()
								.writeValueAsString(operationResult));
			} catch (Throwable e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			throw searchException;
		}

		List<ScoredDocument> documents = new ArrayList<>();
		Cursor cursor = null;
		boolean hasNext = false;
		if (limit > 0) {
			Iterator<ScoredDocument> iterator = searchResutls.iterator();
			int count = 0;
			while (true) {
				hasNext = iterator.hasNext();
				if (hasNext == false || count == limit) {
					break;
				}
				ScoredDocument next = iterator.next();
				documents.add(next);
				cursor = next.getCursor();
				count++;
			}
		} else {
			Iterator<ScoredDocument> iterator = searchResutls.iterator();
			while (true) {
				hasNext = iterator.hasNext();
				if (hasNext == false) {
					break;
				}
				ScoredDocument next = iterator.next();
				documents.add(next);
				cursor = next.getCursor();
			}
		}
		final String cursorWetSafeString = cursor == null ? null : cursor
				.toWebSafeString();
		SearchResultList<ScoredDocument> results = new SearchResultList<ScoredDocument>(
				documents, cursorWetSafeString, hasNext);

		return results;
	}

	Index getIndex() {
		return SearchServiceFactory.getSearchService().getIndex(
				IndexSpec.newBuilder().setName("Item").build());
	}
}
