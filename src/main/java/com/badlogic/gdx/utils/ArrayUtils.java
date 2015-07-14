package com.badlogic.gdx.utils;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * Array utility functions.
 */
public class ArrayUtils {

	/**
	 * Returns an {@link Iterable} interface wrapped around an array.
	 *
	 * <pre>
	 * {@code
	 * Object[] someArray = ...;
	 * ArrayUtils.asIterable(someArray).forEach(element -> {});
	 * }
	 * </pre>
	 */
	public static <T> Iterable<T> asIterable(T[] array) {
		return () -> new ArrayIterator<>(array);
	}

	/**
	 * Returns a second interface to the {@link Iterable} passed.
	 * <p>
	 * The argument must be the result of a previous call to this function, or to
	 * {@link ArrayUtils#asIterable(Object[])}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> asIterable(Iterable<T> iterable) {
		if (iterable instanceof ArrayIterator<?>) {
			return () -> new ArrayIterator<>((ArrayIterator<T>) iterable);
		}
		throw new IllegalArgumentException("Iterable must be of equal type.");
	}

	private static class ArrayIterator<T> implements Iterator<T> {

		private final T[] array;
		private int index = 0;

		ArrayIterator(T[] array) {
			this.array = array;
		}

		ArrayIterator(ArrayIterator<T> other) {
			this.array = other.array;
		}

		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		@Override
		public T next() {
			return array[index++];
		}
	}

	/**
	 * Returns a {@link Collection} interface wrapped around an array.
	 * <p>
	 * The collection returned does not support any operation manipulating the array. Its main purpose (and
	 * difference to {@link ArrayUtils#asIterable(Object[])}) is to expose the {@link Collection#size()} function.
	 */
	public static <T> Collection<T> asCollection(T[] array) {
		return new ArrayCollection<>(array);
	}

	/**
	 * Returns a second interface to the {@link Collection} passed.
	 * <p>
	 * The argument must be the result of a previous call to this function, or to
	 * {@link ArrayUtils#asCollection(Object[])}.
	 */
	public static <T> Collection<T> asCollection(Collection<T> collection) {
		if (collection instanceof ArrayCollection<?>) {
			return new ArrayCollection<>((ArrayCollection<T>) collection);
		}
		throw new IllegalArgumentException("Collection must be of equal type.");
	}

	private static class ArrayCollection<T> extends AbstractCollection<T> {

		private final T[] array;

		ArrayCollection(T[] array) {
			this.array = array;
		}

		ArrayCollection(ArrayCollection<T> other) {
			this.array = other.array;
		}

		@Override
		public Iterator<T> iterator() {
			return new ArrayIterator<>(array);
		}

		@Override
		public int size() {
			return array.length;
		}
	}

}
