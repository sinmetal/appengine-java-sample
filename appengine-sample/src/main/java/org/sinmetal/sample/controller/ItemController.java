package org.sinmetal.sample.controller;

import java.util.*;
import java.util.concurrent.*;

import javax.servlet.http.*;

import org.codehaus.jackson.map.*;
import org.sinmetal.sample.meta.*;
import org.sinmetal.sample.model.*;
import org.sinmetal.sample.service.*;
import org.slim3.controller.*;
import org.slim3.datastore.*;
import org.slim3.util.*;

import com.google.appengine.api.datastore.*;

/**
 * {@link Item} に関するAPI
 * 
 * @author sinmetal
 *
 */
public class ItemController extends AbstructController {

	public static final String PATH = "/item";

	@Override
	protected Navigation run() throws Exception {
		if (isPost()) {
			doPost();
			return null;
		} else if (isPut()) {
			doPut();
			return null;
		} else if (isDelete()) {
			doDelete();
			return null;
		} else if (isGet()) {
			String strKey = request.getParameter("strKey");
			if (StringUtil.isEmpty(strKey)) {
				list();
			} else {
				doGet(strKey);
			}
			return null;
		} else {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return null;
		}
	}

	void doPost() throws Exception {
		final ObjectMapper om = new ObjectMapper();

		PostForm form;
		try {
			form = om.readValue(request.getInputStream(), PostForm.class);
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		List<String> errors = form.validate();
		if (errors.isEmpty() == false) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;
		}

		// FIXME Item作成者のEmailをLoginUserのものに修正する
		Item stored = ItemService.create("user@example.com", form);

		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setContentType(APPLICATION_JSON);
		response.setCharacterEncoding(UTF8);
		response.getWriter().write(ItemMeta.get().modelToJson(stored));
		response.flushBuffer();
	}

	public static class PostForm {

		/** 記事タイトル */
		public String title;

		/** 本文 */
		public String content;

		/**
		 * validate
		 * 
		 * errorが無い時は、空のListを返す
		 * 
		 * @return error message list
		 */
		public List<String> validate() {
			List<String> errors = new ArrayList<>();
			if (StringUtil.isEmpty(title)) {
				errors.add("title is required.");
			}
			return errors;
		}
	}

	void doPut() throws Exception {
		final ObjectMapper om = new ObjectMapper();

		String strKey = request.getParameter("strKey");
		if (StringUtil.isEmpty(strKey)) {
			List<String> errors = new ArrayList<>();
			errors.add("key is required");

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;
		}

		Key key;
		try {
			key = Datastore.stringToKey(strKey);
		} catch (IllegalArgumentException e) {
			List<String> errors = new ArrayList<>();
			errors.add("invalid key");

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;
		}

		PutForm form;
		try {
			form = om.readValue(request.getInputStream(), PutForm.class);
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		List<String> errors = form.validate();
		if (errors.isEmpty() == false) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;
		}

		Item stored;
		try {
			stored = ItemService.update(key, form);
		} catch (ConcurrentModificationException e) {
			// 楽観的排他制御による衝突だけでなく、Txがぶつかった場合も、ここに来ることに注意
			errors = new ArrayList<String>();
			errors.add("conflict.");

			response.setStatus(HttpServletResponse.SC_CONFLICT);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;
		} catch (EntityNotFoundRuntimeException e) {
			errors = new ArrayList<>();
			errors.add(strKey + " is not found.");

			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;

		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(APPLICATION_JSON);
		response.setCharacterEncoding(UTF8);
		response.getWriter().write(ItemMeta.get().modelToJson(stored));
		response.flushBuffer();
	}

	public static class PutForm {

		/** 記事タイトル */
		public String title;

		/** 本文 */
		public String content;

		/** 楽観的排他制御用のversion */
		public Long version;

		/**
		 * validate
		 * 
		 * errorが無い時は、空のListを返す
		 * 
		 * @return error message list
		 */
		public List<String> validate() {
			List<String> errors = new ArrayList<>();
			if (StringUtil.isEmpty(title)) {
				errors.add("title is required.");
			}
			if (version == null) {
				errors.add("version is required.");
			}
			return errors;
		}
	}

	void doDelete() throws Exception {
		final ObjectMapper om = new ObjectMapper();

		String strKey = request.getParameter("strKey");
		if (StringUtil.isEmpty(strKey)) {
			List<String> errors = new ArrayList<>();
			errors.add("key is required");

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;
		}

		Key key;
		try {
			key = Datastore.stringToKey(strKey);
		} catch (IllegalArgumentException e) {
			List<String> errors = new ArrayList<>();
			errors.add("invalid key");

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;
		}

		DeleteForm form;
		try {
			form = om.readValue(request.getInputStream(), DeleteForm.class);
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		List<String> errors = form.validate();
		if (errors.isEmpty() == false) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;
		}

		try {
			ItemService.delete(key, form);
		} catch (ConcurrentModificationException e) {
			// 楽観的排他制御による衝突だけでなく、Txがぶつかった場合も、ここに来ることに注意
			errors = new ArrayList<String>();
			errors.add("conflict.");

			response.setStatus(HttpServletResponse.SC_CONFLICT);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;
		} catch (EntityNotFoundRuntimeException e) {
			errors = new ArrayList<>();
			errors.add(strKey + " is not found.");

			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			om.writeValue(response.getWriter(), errors);
			return;
		}

		List<String> messages = new ArrayList<>();
		messages.add("delete done.");
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(APPLICATION_JSON);
		response.setCharacterEncoding(UTF8);
		om.writeValue(response.getWriter(), messages);
		response.flushBuffer();
	}

	public static class DeleteForm {

		/** 楽観的排他制御用のversion */
		public Long version;

		/**
		 * validate
		 * 
		 * errorが無い時は、空のListを返す
		 * 
		 * @return error message list
		 */
		public List<String> validate() {
			List<String> errors = new ArrayList<>();
			if (version == null) {
				errors.add("version is required.");
			}
			return errors;
		}
	}

	void list() throws Exception {
		final String limitParam = request.getParameter("limit");
		final String cursorParam = request.getParameter("cursor");

		int limit = 20;
		if (StringUtil.isEmpty(limitParam) == false) {
			try {
				limit = Integer.parseInt(limitParam);
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"limit is number");
				return;
			}
		}
		QueryResultIterable<Entity> results = ItemService
				.querySortUpdatedAtDescAsync(limit, cursorParam);
		List<Key> keys = new ArrayList<>();
		QueryResultIterator<Entity> iterator = results.iterator();
		Cursor cursor = null;
		while (iterator.hasNext()) {
			Entity next = iterator.next();
			keys.add(next.getKey());
			cursor = iterator.getCursor();
		}
		// sampleとしてのAsyncでget
		Future<List<Item>> itemsFuture = ItemService.getAsync(keys);
		List<Item> list = FutureUtil.getQuietly(itemsFuture);

		Map<String, Object> map = new HashMap<>();
		String cursorWebSafeString = "";
		if (cursor != null) {
			cursorWebSafeString = cursor.toWebSafeString();
		}
		map.put("cursor", cursorWebSafeString);
		map.put("list", list);

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(APPLICATION_JSON);
		response.setCharacterEncoding(UTF8);
		new ObjectMapper().writeValue(response.getOutputStream(), map);
		response.flushBuffer();
	}

	void doGet(String strKey) throws Exception {
		Key key;
		try {
			key = Datastore.stringToKey(strKey);
		} catch (IllegalArgumentException e) {
			List<String> errors = new ArrayList<>();
			errors.add("invalid key");

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			new ObjectMapper().writeValue(response.getWriter(), errors);
			return;
		}

		Item item = ItemService.getOrNull(key);
		if (item == null) {
			List<String> errors = new ArrayList<>();
			errors.add(strKey + " is not found.");

			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(UTF8);
			new ObjectMapper().writeValue(response.getWriter(), errors);
			return;
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(APPLICATION_JSON);
		response.setCharacterEncoding(UTF8);
		response.getWriter().write(ItemMeta.get().modelToJson(item));
		response.flushBuffer();
	}
}
