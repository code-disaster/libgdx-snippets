package com.badlogic.gdx.utils;

import java.util.Iterator;

/**
 * Array utility functions.
 */
public class ArrayUtils {

	/**
	 * Returns a lean iterator for use in forEach() statements.
	 *
	 * <pre>
	 * {@code
	 * Object[] someArray = ...;
	 * ArrayUtils.asIterable(someArray).forEach(element -> {});
	 * }
	 * </pre>
	 */
	public static <T> Iterable<T> asIterable(T[] array) {

		return () -> new Iterator<T>() {
			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < array.length;
			}

			@Override
			public T next() {
				return array[index++];
			}
		};

	}

}
