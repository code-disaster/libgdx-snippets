package com.badlogic.gdx.utils;

/**
 * A specialized version of {@link Pool} which only allows object creation to up to a
 * maximum number of instances. If this maximum is reached, and there are no free
 * objects in the pool, {@link Pool#obtain()} returns null.
 */
public abstract class LimitedPool<T> extends Pool<T> {

	private int limit;

	public LimitedPool(int capacity) {
		super(capacity, capacity);
		limit = capacity;
	}

	public int getLimit() {
		return limit;
	}

	@Override
	public T obtain() {
		if (getFree() == 0) {
			return newObjectWithLimit();
		} else {
			return super.obtain();
		}
	}

	private T newObjectWithLimit() {
		if (limit > 0) {
			limit--;
			return newObject();
		}
		return null;
	}

	/**
	 * "Drains" the pool by obtaining new elements until the maximum capacity is reached.
	 *
	 * This can be used to pre-populate {@link Pool#freeObjects} like this, at the expense
	 * of a (potentially very large) intermediate array storage:
	 * <pre>
	 *     {@code
	 *     Array<PoolObject> array = new Array<>();
	 *     pool.freeAll(pool.drain(array));
	 *     }
	 * </pre>
	 */
	public Array<T> drain(Array<T> array) {
		return drain(array, limit);
	}

	/**
	 * A variant of {@link LimitedPool#drain(Array)} which drains a limited amount of pool
	 * elements, reducing memory usage of intermediate array storage.
	 */
	public Array<T> drain(Array<T> array, int capacity) {

		int count = capacity;

		while (limit > 0 && count > 0) {
			array.add(obtain());
			count--;
		}

		return array;
	}

	/**
	 * Same as {@link Pool#freeAll(Array)}, but freed elements are credited to {@link LimitedPool#limit}.
	 *
	 * This can be used to fill one {@link LimitedPool} with elements drained from another, with both pools
	 * obeying their respective {@link LimitedPool#limit} restrictions:
	 * <pre>
	 *     {@code
	 *     Array<PoolObject> array = new Array<>();
	 *     pool.pour(otherPool.drain(array));
	 *     }
	 * </pre>
	 */
	public void pour(Array<T> array) {
		limit -= array.size;
		freeAll(array);
	}

}
