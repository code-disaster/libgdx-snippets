package com.badlogic.gdx.utils;

import java.util.*;
import java.util.function.Consumer;

/**
 * Array utility functions.
 * <p>
 * Note: The functions to modify arrays are mostly written for convenience, not for efficiency. They should not be
 * used in code sensitive to memory consumption or execution speed.
 */
public class ArrayUtils {

	/**
	 * Convenience function to iterate an array.
	 */
	public static <T> void forEach(T[] array, Consumer<T> action) {
		for (T t : array) {
			action.accept(t);
		}
	}

	/**
	 * Appends an element to a copy of the given array.
	 */
	public static <T> T[] append(T[] array, T element) {
		int len = array.length;
		array = Arrays.copyOf(array, len + 1);
		array[len] = element;
		return array;
	}

	/**
	 * Returns a copy of the given array, with the element at the given index removed.
	 * <p>
	 * This version moves the last array element to the removed element's position.
	 */
	public static <T> T[] removeIndex(T[] array, int index) {
		return removeIndex(array, index, false);
	}

	/**
	 * Returns a copy of the given array, with the element at the given index removed.
	 */
	public static <T> T[] removeIndex(T[] array, int index, boolean keepOrder) {

		if (index != array.length - 1) {
			if (keepOrder) {
				System.arraycopy(array, index + 1, array, index, array.length - 2 - index);
			} else {
				array[index] = array[array.length - 1];
			}
		}

		return Arrays.copyOf(array, array.length - 1);
	}

	/**
	 * Expands an existing two-dimensional array.
	 * <p>
	 * This is a very memory-inefficient operation.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[][] expand(T[][] arrayOfArray, Class<T> clazz,
								   int lowerDim0, int upperDim0, int lowerDim1, int upperDim1) {

		int columns = arrayOfArray.length + lowerDim0 + upperDim0;
		int rows = arrayOfArray[0].length + lowerDim1 + upperDim1;

		Object copy = java.lang.reflect.Array.newInstance(clazz, columns, rows);

		T[][] dest = (T[][]) copy;

		for (int i = lowerDim0; i < columns - upperDim0; i++) {
			for (int j = lowerDim1; j < rows - upperDim1; j++) {
				dest[i][j] = arrayOfArray[i - lowerDim0][j - lowerDim1];
			}
		}

		return dest;
	}

	/**
	 * Combined check for null or empty array.
	 */
	public static <T> boolean isNullOrEmpty(T[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * Returns an {@link Iterable} interface wrapped around an array, in ascending order.
	 */
	public static <T> Iterable<T> asIterable(T[] array) {
		return asIterable(array, false);
	}

	/**
	 * Returns an {@link Iterable} interface wrapped around an array, in ascending or descending order.
	 *
	 * <pre>
	 * {@code
	 * Object[] someArray = ...;
	 * ArrayUtils.asIterable(someArray, false).forEach(element -> {});
	 * }
	 * </pre>
	 */
	public static <T> Iterable<T> asIterable(T[] array, boolean descending) {
		return () -> descending ? new DescendingArrayIterator<>(array) : new ArrayIterator<>(array);
	}

	/**
	 * Returns a second interface to the {@link Iterable} passed, in ascending order.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> asIterable(Iterable<T> iterable) {
		return asIterable(iterable, false);
	}

	/**
	 * Returns a second interface to the {@link Iterable} passed.
	 * <p>
	 * The argument must be the result of a previous call to this function, or to
	 * {@link ArrayUtils#asIterable(Object[], boolean)}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> asIterable(Iterable<T> iterable, boolean descending) {
		if (iterable instanceof ArrayIterator<?>) {
			return () -> descending
					? new DescendingArrayIterator<>((AbstractArrayIterator<T>) iterable)
					: new ArrayIterator<>((AbstractArrayIterator<T>) iterable);
		}
		throw new IllegalArgumentException("Iterable must be of equal type.");
	}

	private abstract static class AbstractArrayIterator<T> implements Iterator<T> {

		protected final T[] array;
		protected int index = 0;

		AbstractArrayIterator(T[] array) {
			this.array = array;
		}

	}

	private static class ArrayIterator<T> extends AbstractArrayIterator<T> {

		ArrayIterator(T[] array) {
			super(array);
		}

		ArrayIterator(AbstractArrayIterator<T> other) {
			super(other.array);
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

	private static class DescendingArrayIterator<T> extends AbstractArrayIterator<T> {

		DescendingArrayIterator(T[] array) {
			super(array);
			index = array.length - 1;
		}

		DescendingArrayIterator(AbstractArrayIterator<T> other) {
			super(other.array);
			index = other.array.length - 1;
		}

		@Override
		public boolean hasNext() {
			return index >= 0;
		}

		@Override
		public T next() {
			return array[index--];
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
