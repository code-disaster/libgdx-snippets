package com.badlogic.gdx.lang;

/**
 * Utility class to box a typed value. Primarily meant for capturing non-final variables for use in lambda functions.
 *
 * <pre>
 * {@code
 * int getSum(Iterable<Integer> container) {
 *     final Box<Integer> sum = new Box<>(() -> 0);
 *     container.forEach(element -> sum.value += element);
 *     return sum.value;
 * }
 * }
 * </pre>
 *
 * @param <T> Type of the boxed value.
 */
public final class Box<T> {

	@FunctionalInterface
	public interface Assign<T> {
		T value();
	}

	public T value;

	public Box() {

	}

	public Box(Assign<T> initialValue) {
		value = initialValue.value();
	}

}
