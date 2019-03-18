package com.badlogic.gdx.lang;

import com.badlogic.gdx.concurrent.ThreadLocalInstance;
import com.badlogic.gdx.function.Consumer;

import java.lang.reflect.Array;

/**
 * Utility class to box typed values. Can be used to capture non-final
 * variables for lambda functions.
 *
 * <pre>
 * {@code
 * int getSum(Iterable<Integer> container) {
 *     final Box.Integer sum = new Box.Integer(0);
 *     container.forEach(element -> sum.set(sum.get() + element));
 *     return sum.get();
 * }
 * }
 * </pre>
 *
 * For boxed primitive types there is a small, thread-local storage which holds
 * one per-thread instance of each sub-class.
 *
 * <pre>
 * {@code
 * int getSum(Iterable<Integer> container) {
 *     final Box.Integer sum = Box.borrowInteger();
 *     sum.set(0);
 *     container.forEach(element -> sum.set(sum.get() + element));
 *     int result = sum.get();
 *     Box.releaseInteger();
 *     return result;
 * }
 * }
 * </pre>
 *
 * There's also a version which uses try-with-resources internally.
 *
 * <pre>
 * {@code
 * int getSum(Iterable<Integer> container) {
 * 	   return Box.withInteger(sum -> {
 *         sum.set(0);
 *         container.forEach(element -> sum.set(sum.get() + element));
 * 	   });
 * }
 * }
 * </pre>
 *
 */
public final class Box {

	public static final class Boolean {

		private boolean value;

		@SuppressWarnings("unused")
		Boolean() {
			this.value = false;
		}

		public Boolean(boolean value) {
			this.value = value;
		}

		public boolean get() {
			return value;
		}

		public Boolean set(boolean value) {
			this.value = value;
			return this;
		}

	}

	public static final class Integer {

		private int value;

		@SuppressWarnings("unused")
		Integer() {
			this.value = 0;
		}

		public Integer(int value) {
			this.value = value;
		}

		public int get() {
			return value;
		}

		public int getAndIncrement() {
			return value++;
		}

		public Integer set(int value) {
			this.value = value;
			return this;
		}

		public Integer add(int value) {
			this.value += value;
			return this;
		}

	}

	public static final class Float {

		private float value;

		@SuppressWarnings("unused")
		Float() {
			this.value = 0.0f;
		}

		public Float(float value) {
			this.value = value;
		}

		public float get() {
			return value;
		}

		public Float set(float value) {
			this.value = value;
			return this;
		}

	}

	public static final class Reference<R> {

		private R value;

		public Reference(R value) {
			this.value = value;
		}

		public R get() {
			return value;
		}

		public boolean isNull() {
			return value == null;
		}

		public Reference<R> set(R value) {
			this.value = value;
			return this;
		}

	}

	private static class BorrowChecker<B> implements AutoCloseable {

		private final B[] references;
		private int locks = 0;

		private static final int cacheSize = 4;

		@SuppressWarnings("unchecked")
		private BorrowChecker(Class<B> clazz) {
			try {
				references = (B[]) Array.newInstance(clazz, cacheSize);
				for (int i = 0; i < cacheSize; i++) {
					references[i] = clazz.newInstance();
				}
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		BorrowChecker<B> borrow() {
			if (locks >= cacheSize) {
				throw new RuntimeException("Too many nested borrows!");
			}
			locks++;
			return this;
		}

		B reference() {
			return references[locks - 1];
		}

		@Override
		public void close() {
			locks--;
		}

	}

	private static final ThreadLocal<BorrowChecker<Boolean>> tlsBoolean =
			new ThreadLocalInstance<>(() -> new BorrowChecker<>(Boolean.class));

	private static final ThreadLocal<BorrowChecker<Integer>> tlsInteger =
			new ThreadLocalInstance<>(() -> new BorrowChecker<>(Integer.class));

	private static final ThreadLocal<BorrowChecker<Float>> tlsFloat =
			new ThreadLocalInstance<>(() -> new BorrowChecker<>(Float.class));

	public static Boolean borrowBoolean() {
		return tlsBoolean.get().borrow().reference();
	}

	public static boolean releaseBoolean() {
		BorrowChecker<Boolean> value = tlsBoolean.get();
		boolean result = value.reference().get();
		value.close();
		return result;
	}

	public static boolean withBoolean(Consumer<Boolean> consumer) {
		try (BorrowChecker<Boolean> value = tlsBoolean.get().borrow()) {
			consumer.accept(value.reference());
			return value.reference().get();
		}
	}

	public static Integer borrowInteger() {
		return tlsInteger.get().borrow().reference();
	}

	public static int releaseInteger() {
		BorrowChecker<Integer> value = tlsInteger.get();
		int result = value.reference().get();
		value.close();
		return result;
	}

	public static int withInteger(Consumer<Integer> consumer) {
		try (BorrowChecker<Integer> value = tlsInteger.get().borrow()) {
			consumer.accept(value.reference());
			return value.reference().get();
		}
	}

	public static float withFloat(Consumer<Float> consumer) {
		try (BorrowChecker<Float> value = tlsFloat.get().borrow()) {
			consumer.accept(value.reference());
			return value.reference().get();
		}
	}

}
