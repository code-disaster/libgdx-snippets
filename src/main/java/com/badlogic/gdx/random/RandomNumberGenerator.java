package com.badlogic.gdx.random;

public interface RandomNumberGenerator {

	long next();

	void getSeed(long[] seed);

}
