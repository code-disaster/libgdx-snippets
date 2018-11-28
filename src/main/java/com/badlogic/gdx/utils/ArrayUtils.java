package com.badlogic.gdx.utils;

import com.badlogic.gdx.function.Consumer;
import com.badlogic.gdx.function.Predicate;
import com.badlogic.gdx.random.RandomNumbers;

import java.util.*;

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
	 * Convenience function to iterate an array. Stops if user-supplied function returns false.
	 * <p>
	 * @return true if end of array was reached
	 */
	public static <T> boolean forEachWhile(T[] array, Predicate<T> predicate) {
		for (T t : array) {
			if (!predicate.test(t)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Convenience function to iterate an array. Stops if user-supplied function returns false.
	 * <p>
	 * @return true if end of array was reached
	 */
	public static <T> boolean forEachWhile(Array<T> array, Predicate<T> action) {
		for (T t : array) {
			if (!action.test(t)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Iterates the array, and returns the first item which fulfills the user-defined comparison.
	 * <p>
	 * Returns null if no match is found.
	 */
	public static <T> T find(T[] array, Predicate<T> match) {
		for (T t : array) {
			if (match.test(t)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Iterates the array, and returns the first item which fulfills the user-defined comparison.
	 * <p>
	 * Returns null if no match is found.
	 */
	public static <T> T find(Array<T> array, Predicate<T> match) {
		for (T t : array) {
			if (match.test(t)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Iterates the array, and returns the index of the first item which fulfills the user-defined comparison.
	 * <p>
	 * Returns -1 if no match is found.
	 */
	public static <T> int findIndex(T[] array, Predicate<T> match) {
		for (int i = 0; i < array.length; i++) {
			if (match.test(array[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Iterates the array, and returns the index of the first item which fulfills the user-defined comparison.
	 * <p>
	 * Returns -1 if no match is found.
	 */
	public static <T> int findIndex(Array<T> array, Predicate<T> match) {
		for (int i = 0; i < array.size; i++) {
			if (match.test(array.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Iterates the array, consuming items only which fulfill the user-defined comparison.
	 */
	public static <T> void findAll(T[] array, Predicate<T> match, Consumer<T> action) {
		for (T t : array) {
			if (match.test(t)) {
				action.accept(t);
			}
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
			/*for (int j = lowerDim1; j < rows - upperDim1; j++) {
				dest[i][j] = arrayOfArray[i - lowerDim0][j - lowerDim1];
			}*/
			System.arraycopy(arrayOfArray[i - lowerDim0], 0, dest[i], lowerDim1, rows - upperDim1 - lowerDim1);
		}

		return dest;
	}

	/**
	 * Expands an existing three-dimensional array.
	 * <p>
	 * This is a very memory-inefficient operation.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[][][] expand(T[][][] arrayOfArrayOfArray, Class<T> clazz,
								   int lowerDim0, int upperDim0, int lowerDim1, int upperDim1) {

		int columns = arrayOfArrayOfArray.length + lowerDim0 + upperDim0;
		int rows = arrayOfArrayOfArray[0].length + lowerDim1 + upperDim1;
		int depth = arrayOfArrayOfArray[0][0].length;

		Object copy = java.lang.reflect.Array.newInstance(clazz, columns, rows, depth);

		T[][][] dest = (T[][][]) copy;

		for (int i = lowerDim0; i < columns - upperDim0; i++) {
			for (int j = lowerDim1; j < rows - upperDim1; j++) {
				/*for (int k = 0; k < depth; k++) {
					dest[i][j][k] = arrayOfArrayOfArray[i - lowerDim0][j - lowerDim1][k];
				}*/
				System.arraycopy(
						arrayOfArrayOfArray[i - lowerDim0][j - lowerDim1],
						0,
						dest[i][j],
						0,
						depth);
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
	 * Returns an {@link Iterable} interface wrapped around an {@link Array}, in ascending order.
	 */
	public static <T> Iterable<T> asIterable(Array<T> array) {
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
		return () -> descending
				? new DescendingArrayIterator<>(array, array.length)
				: new ArrayIterator<>(array, array.length);
	}

	/**
	 * Returns an {@link Iterable} interface wrapped around an {@link Array}, in ascending or descending order.
	 */
	public static <T> Iterable<T> asIterable(Array<T> array, boolean descending) {
		return () -> descending
				? new DescendingArrayIterator<>(array.items, array.size)
				: new ArrayIterator<>(array.items, array.size);
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
		protected final int size;
		protected int index = 0;

		AbstractArrayIterator(T[] array, int size) {
			this.array = array;
			this.size = size;
		}

	}

	private static class ArrayIterator<T> extends AbstractArrayIterator<T> {

		ArrayIterator(T[] array, int size) {
			super(array, size);
		}

		ArrayIterator(AbstractArrayIterator<T> other) {
			super(other.array, other.size);
		}

		@Override
		public boolean hasNext() {
			return index < size;
		}

		@Override
		public T next() {
			if (index < size) {
				return array[index++];
			}
			throw new NoSuchElementException("Out of bounds!");
		}
	}

	private static class DescendingArrayIterator<T> extends AbstractArrayIterator<T> {

		DescendingArrayIterator(T[] array, int size) {
			super(array, size);
			index = this.size - 1;
		}

		DescendingArrayIterator(AbstractArrayIterator<T> other) {
			super(other.array, other.size);
			index = other.size - 1;
		}

		@Override
		public boolean hasNext() {
			return index >= 0;
		}

		@Override
		public T next() {
			if (index >= 0) {
				return array[index--];
			}
			throw new NoSuchElementException("Out of bounds!");
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
	 * Returns a {@link Collection} interface wrapped around an {@link Array}.
	 * <p>
	 * The collection returned does not support any operation manipulating the array. Its main purpose (and
	 * difference to {@link ArrayUtils#asIterable(Object[])}) is to expose the {@link Collection#size()} function.
	 */
	public static <T> Collection<T> asCollection(Array<T> array) {
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
		private final int size;

		ArrayCollection(T[] array) {
			this.array = array;
			this.size = array.length;
		}

		ArrayCollection(Array<T> array) {
			this.array = array.items;
			this.size = array.size;
		}

		ArrayCollection(ArrayCollection<T> other) {
			this.array = other.array;
			this.size = other.size;
		}

		@Override
		public Iterator<T> iterator() {
			return new ArrayIterator<>(array, size);
		}

		@Override
		public int size() {
			return size;
		}
	}

	/**
	 * Shuffles an array, just like {@link Array#shuffle()}, but with a user-provided
	 * random number generator.
	 */
	public static <T> void shuffle(Array<T> array, RandomNumbers random) {
		shuffle(array.items, array.size - 1, random);
	}

	/**
	 * Shuffles an array with a user-provided random number generator.
	 */
	public static <T> void shuffle(T[] array, int length, RandomNumbers random) {
		for (int i = length - 1; i > 0; i--) {
			int ii = random.nextInt(i);
			if (i != ii) {
				T temp = array[i];
				array[i] = array[ii];
				array[ii] = temp;
			}
		}
	}

	public static <T> Array<T> singleton(T element) {
		Array<T> array = new Array<>();
		array.add(element);
		return array;
	}

}
