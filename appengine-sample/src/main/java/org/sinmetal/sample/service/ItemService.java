package org.sinmetal.sample.service;

import java.util.*;
import java.util.concurrent.*;

import org.sinmetal.sample.controller.ItemController.DeleteForm;
import org.sinmetal.sample.controller.ItemController.PostForm;
import org.sinmetal.sample.controller.ItemController.PutForm;
import org.sinmetal.sample.meta.*;
import org.sinmetal.sample.model.*;
import org.slim3.datastore.*;
import org.slim3.util.*;

import com.google.appengine.api.datastore.*;

/**
 * {@link Item} のユーティリティ
 * 
 * @author sinmetal
 *
 */
public class ItemService {

	static ItemMeta meta = ItemMeta.get();

	private ItemService() {
	};

	/**
	 * {@link Key} 生成
	 * 
	 * @param uuid
	 *            UUID
	 * @return {@link Item} {@link Key}
	 */
	public static Key createKey(String uuid) {
		return Datastore.createKey(meta, uuid);
	}

	/**
	 * {@link Item} 生成
	 * 
	 * @param item
	 */
	public static Item put(Item item) {
		Datastore.put(item);
		return item;
	}

	/**
	 * 新しい {@link Item} を作成する
	 * 
	 * @param email
	 *            作成者のemail
	 * @param form
	 *            RequestData
	 * @return 作成した {@link Item}
	 */
	public static Item create(String email, PostForm form) {
		Key key = createKey(UUID.randomUUID().toString());
		Item item = new Item();
		item.setKey(key);
		item.setEmail(email);
		item.setTitle(form.title);
		item.setContent(form.content);
		Datastore.put(item);
		return item;
	}

	/**
	 * 指定した {@link Key} の {@link Item} を更新する
	 * 
	 * @param key
	 *            {@link Item} {@link Key}
	 * @param form
	 *            RequestData
	 * @return 更新した {@link Item}
	 */
	public static Item update(Key key, PutForm form) {
		Transaction tx = Datastore.beginTransaction();
		try {
			Item item = Datastore.get(tx, meta, key, form.version);
			item.setTitle(form.title);
			item.setContent(form.content);
			Datastore.put(item);
			tx.commit();

			return item;
		} finally {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		}
	}

	/**
	 * 指定した {@link Key} の {@link Item} を削除する
	 * 
	 * @param key
	 *            {@link Item} {@link Key}
	 * @param form
	 *            RequestData
	 */
	public static void delete(Key key, DeleteForm form) {
		Transaction tx = Datastore.beginTransaction();
		try {
			Datastore.get(tx, meta, key, form.version);
			Datastore.delete(key);
			tx.commit();
		} finally {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		}
	}

	/**
	 * {@link Item} を更新日時の降順で全件取得する
	 * 
	 * Index遅延を考慮した用心深い実装になっている。
	 * 
	 * @return 更新日時の降順の {@link Item} 全件
	 */
	public static List<Item> querySortUpdatedAtDesc() {
		List<Key> keys = Datastore.query(meta).sort(meta.updatedAt.desc)
				.asKeyList();
		Map<Key, Item> map = Datastore.getAsMap(meta, keys);

		List<Item> results = new ArrayList<>();
		for (Key key : keys) {
			if (map.containsKey(key)) {
				results.add(map.get(key));
			}
		}
		return results;
	}

	/**
	 * {@link Item} を更新日時の降順で指定数取得する
	 * 
	 * Index遅延を考慮した用心深い実装になっている。
	 * 
	 * @return 更新日時の降順の {@link Item}
	 */
	public static S3QueryResultList<Item> querySortUpdatedAtDesc(int limit) {
		S3QueryResultList<Item> list = Datastore.query(meta).limit(limit)
				.sort(meta.updatedAt.desc).asQueryResultList();
		List<Key> keys = new ArrayList<>();
		for (Item item : list) {
			keys.add(item.getKey());
		}
		Map<Key, Item> map = Datastore.getAsMap(meta, keys);

		List<Item> results = new ArrayList<>();
		for (Key key : keys) {
			if (map.containsKey(key)) {
				results.add(map.get(key));
			}
		}
		return new S3QueryResultList<>(results, list.getEncodedCursor(),
				list.getEncodedFilter(), list.getEncodedSorts(), list.hasNext());
	}

	/**
	 * {@link Item} を更新日時の降順で指定数取得する
	 * 
	 * Index遅延を考慮した用心深い実装になっている。
	 * 
	 * @return 更新日時の降順の {@link Item} 全件
	 */

	/**
	 * {@link Item} を更新日時の降順で指定数取得する
	 * 
	 * @param limit
	 * @param encodedCursor
	 * @return 更新日時の降順の QueryResultIterable<Entity> (keysOnly)
	 */
	public static QueryResultIterable<Entity> querySortUpdatedAtDescAsync(
			int limit, String encodedCursor) {
		ModelQuery<Item> q = Datastore.query(meta);
		if (StringUtil.isEmpty(encodedCursor) == false) {
			q = q.startCursor(Cursor.fromWebSafeString(encodedCursor));
		}
		return q.sort(meta.updatedAt.desc).limit(limit)
				.asQueryResultEntityIterable();
	}

	/**
	 * {@link Item} の一覧を取得
	 * 
	 * @param keys
	 *            {@link Item} {@link Key}
	 * @return {@link Item} の一覧
	 */
	public static List<Item> get(List<Key> keys) {
		return Datastore.get(meta, keys);
	}

	/**
	 * {@link Item} の一覧を非同期で取得
	 * 
	 * @param keys
	 *            {@link Item} {@link Key}
	 * @return {@link Item} の一覧 のFuture
	 */
	public static Future<List<Item>> getAsync(List<Key> keys) {
		return Datastore.getAsync(meta, keys);
	}

	/**
	 * 指定した {@link Key} の {@link Item} を取得する。
	 * 
	 * @param key
	 *            {@link Item} {@link Key}
	 * @return {@link Item} or null
	 */
	public static Item getOrNull(Key key) {
		return Datastore.getOrNull(meta, key);
	}
}
