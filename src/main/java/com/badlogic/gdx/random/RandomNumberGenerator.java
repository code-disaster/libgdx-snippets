package com.badlogic.gdx.random;

public interface RandomNumberGenerator {

	long next();

	void seed(long s0, long s1);

	void getSeed(long[] seed);

}
