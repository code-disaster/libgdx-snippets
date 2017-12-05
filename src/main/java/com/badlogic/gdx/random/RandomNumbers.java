package com.badlogic.gdx.random;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;

public final class RandomNumbers {

	private final RandomNumberGenerator generator;

	public RandomNumbers(RandomXS128 generator) {
		this(new RandomNumberGenerator() {
			@Override
			public long next() {
				return generator.nextLong();
			}

			@Override
			public void getSeed(long[] seed) {
				seed[0] = generator.getState(0);
				seed[1] = generator.getState(1);
			}
		});
	}

	public RandomNumbers(RandomNumberGenerator generator) {
		this.generator = generator;
	}

	public int nextInt() {
		return (int) generator.next();
	}

	public int nextInt(final int n) {
		return (int) nextLongExclusive(n + 1);
	}

	public int nextInt(int start, int end) {
		return start + nextInt(end - start);
	}

	public long nextLong() {
		return generator.next();
	}

	public long nextLong(long n) {
		return nextLongExclusive(n + 1);
	}

	private long nextLongExclusive(final long n) {

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
	 * Pick a random array member. Returns null if the array is empty.
	 */
	public <T> T nextInArray(Array<T> array) {

		if (array.size == 0) {
			return null;
		}

		return array.get(nextInt(array.size - 1));
	}

	/**
	 * Uses reflection to return a random enum value. Has a relatively large runtime overhead.
	 */
	public <T extends Enum<T>> T nextEnum(Class<T> enumType) {
		T[] values = enumType.getEnumConstants();
		return values[nextInt(values.length - 1)];
	}

	public void getSeed(long[] seed) {
		generator.getSeed(seed);
	}

}
