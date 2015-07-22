package org.sinmetal.sample.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * SearchApiResult<br>
 * SearchApiの検索結果にcursorとhasNextを搭載したもの
 * 
 * @author sinmetal
 * @param <T>
 */
public class SearchResultList<T> implements List<T>, Serializable {

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param delegate
	 *            the delegate
	 * @throws NullPointerException
	 *             if the delegate parameter is null
	 */
	public SearchResultList(List<T> delegate) throws NullPointerException {
		if (delegate == null) {
			throw new NullPointerException(
					"The delegate parameter must not be null.");
		}
		this.delegate = delegate;
	}

	/**
	 * Constructor.
	 * 
	 * @param delegate
	 *            the delegate
	 * @param encodedCursor
	 *            the cursor
	 * @param hasNext
	 * @throws NullPointerException
	 *             if the delegate parameter is null
	 */
	public SearchResultList(List<T> delegate, String encodedCursor,
			boolean hasNext) throws NullPointerException {
		if (delegate == null) {
			throw new NullPointerException(
					"The delegate parameter must not be null.");
		}
		this.delegate = delegate;
		this.encodedCursor = encodedCursor;
		this.hasNext = hasNext;
	}

	/**
	 * The delegate.
	 */
	protected List<T> delegate;

	/**
	 * The cursor as encoded string.
	 */
	protected String encodedCursor;

	/**
	 * Whether a next entry exists.
	 */
	protected boolean hasNext;

	/**
	 * @return the encodedCursor
	 * @category accessor
	 */
	public String getEncodedCursor() {
		return encodedCursor;
	}

	/**
	 * @return the hasNext
	 * @category accessor
	 */
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return delegate.iterator();
	}

	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	@Override
	public <A> A[] toArray(A[] a) {
		return delegate.toArray(a);
	}

	@Override
	public boolean add(T e) {
		return delegate.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return delegate.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return delegate.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return delegate.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return delegate.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return delegate.retainAll(c);
	}

	@Override
	public void clear() {
		delegate.clear();

	}

	@Override
	public T get(int index) {
		return delegate.get(index);
	}

	@Override
	public T set(int index, T element) {
		return delegate.set(index, element);
	}

	@Override
	public void add(int index, T element) {
		delegate.add(index, element);

	}

	@Override
	public T remove(int index) {
		return delegate.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return delegate.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return delegate.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return delegate.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return delegate.subList(fromIndex, toIndex);
	}

}
