package com.badlogic.gdx.random;

import com.badlogic.gdx.math.RandomXS128;

public final class RandomNumbers {

	private final RandomNumberGenerator generator;

	public RandomNumbers(RandomXS128 generator) {
		this(generator::nextLong);
	}

	public RandomNumbers(RandomNumberGenerator generator) {
		this.generator = generator;
	}

	public int nextInt() {
		return (int) generator.next();
	}

	public int nextInt(final int n) {
		return (int) nextLong(n);
	}

	public int nextInt(int start, int end) {
		return start + nextInt(end - start + 1);
	}

	public long nextLong() {
		return generator.next();
	}

	public long nextLong(final long n) {

		if (n <= 0) {
			throw new IllegalArgumentException("n must be positive");
		}

		long t = generator.next();
		final long nMinus1 = n - 1;

		if ((n & nMinus1) == 0) {
			return t & nMinus1;
		}

		for (long u = t >>> 1; u + nMinus1 - (t = u % n) < 0; u = generator.next() >>> 1) {
			/* empty */
		}

		return t;
	}

	public double nextDouble() {
		return Double.longBitsToDouble(generator.next() >>> 12 | 0x3FFL << 52) - 1.0;
	}

	public float nextFloat() {
		return Float.intBitsToFloat((int) (generator.next() >>> 41) | 0x3F8 << 20) - 1.0f;
	}

	public boolean nextBoolean() {
		return (generator.next() & 1) != 0;
	}

	/**
	 * Uses reflection to return a random enum value. Has a relatively large runtime overhead.
	 */
	public <T extends Enum<T>> T nextEnum(Class<T> enumType) {
		T[] values = enumType.getEnumConstants();
		return values[nextInt(values.length)];
	}

}
