package com.badlogic.gdx.random;

/**
 * Slim Java implementation of the xoroshiro128plus pseudo-random number generator
 * written in 2016 by David Blackman and Sebastiano Vigna.
 *
 * http://xoroshiro.di.unimi.it/
 * http://dsiutils.di.unimi.it/
 */
public final class XoRoShiRo128Plus implements RandomNumberGenerator {

	private long s0, s1;
	private final static long[] JUMP = { 0xbeac0467eba5facbL, 0xd86b048b86aa9922L };

	/**
	 * "The state must be seeded so that it is not everywhere zero."
	 */
	public XoRoShiRo128Plus(long s0, long s1) {
		seed(s0, s1);
	}

	@Override
	public long next() {
		long s0 = this.s0;
		long s1 = this.s1;
		long result = s0 + s1;

		s1 ^= s0;
		this.s0 = Long.rotateLeft(s0, 55) ^ s1 ^ (s1 << 14);
		this.s1 = Long.rotateLeft(s1, 36);

		return result;
	}

	public void jump() {
		long s0 = 0;
		long s1 = 0;

		for (int i = 0; i < JUMP.length; i++) {
			for (int b = 0; b < 64; b++) {
				if ((JUMP[i] & 1L << b) != 0) {
					s0 ^= this.s0;
					s1 ^= this.s1;
				}
				next();
			}
		}

		this.s0 = s0;
		this.s1 = s1;
	}

	public void seed(long s0, long s1) {
		this.s0 = s0;
		this.s1 = s1;
	}

	public void getSeed(long[] out) {
		out[0] = s0;
		out[1] = s1;
	}

}

