package com.badlogic.gdx.checksum;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;

import java.security.*;
import java.util.Arrays;
import java.util.PrimitiveIterator;

public class SHA1 {

	private final byte[] value = new byte[20];

	public static final SHA1 Zero = new SHA1();

	private SHA1() {

	}

	private SHA1(byte[] value) {
		System.arraycopy(value, 0, this.value, 0, 20);
	}

	private SHA1(CharSequence value) {

		if (value.length() != 40) {
			throw new GdxRuntimeException("");
		}

		PrimitiveIterator.OfInt chars = value.chars().iterator();

		for (int i = 0; i < 20; i++) {

			int upper = chars.nextInt();
			upper = (upper >= 'a') ? (upper - 'a' + 10) : (upper - '0');
			int lower = chars.nextInt();
			lower = (lower >= 'a') ? (lower - 'a' + 10) : (lower - '0');

			int v = (upper << 4) | lower;

			this.value[i] = (byte) v;
		}
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}

	public boolean equals(SHA1 value) {
		return Arrays.equals(this.value, value.value);
	}

	@Override
	public boolean equals(Object object) {
		return (object instanceof SHA1) && equals((SHA1) object);
	}

	public static boolean isNullOrZero(SHA1 value) {
		return value == null || Zero.equals(value);
	}

	public static SHA1 valueOf(byte[] value) {
		return new SHA1(value);
	}

	public static SHA1 valueOf(CharSequence value) {
		return new SHA1(value);
	}

	public static SHA1 create() {
		SHA1 value = new SHA1();
		reset(value);
		return value;
	}

	public static SHA1 reset(SHA1 value) {

		if (!algorithm.isCurrent(null)) {
			throw new GdxRuntimeException("");
		}

		algorithm.makeCurrent(value);
		algorithm.digest.get().reset();

		return value;
	}

	public static SHA1 update(SHA1 value, byte[] buffer, int offset, int length) {

		if (!algorithm.isCurrent(value)) {
			throw new GdxRuntimeException("");
		}

		algorithm.digest.get().update(buffer, offset, length);

		return value;
	}

	public static SHA1 submit(SHA1 value) {

		if (!algorithm.isCurrent(value)) {
			throw new GdxRuntimeException("");
		}

		try {
			algorithm.digest.get().digest(value.value, 0, 20);
		} catch (DigestException e) {
			throw new GdxRuntimeException(e);
		}

		algorithm.makeCurrent(null);

		return value;
	}

	@Override
	public String toString() {

		StringBuilder builder = algorithm.stringBuilder.get();
		builder.setLength(0);

		int v;
		char c;

		for (byte b : value) {
			v = (b & 0xf0) >> 4;
			c = (v < 10) ? (char) ('0' + v) : (char) ('a' + v - 10);
			builder.append(c);
			v = b & 0x0f;
			c = (v < 10) ? (char) ('0' + v) : (char) ('a' + v - 10);
			builder.append(c);
		}

		return builder.toString();
	}

	/**
	 * (Re-)use thread-local instances of {@link MessageDigest}
	 */
	private static class Algorithm {

		ThreadLocal<MessageDigest> digest = new ThreadLocal<MessageDigest>() {
			@Override
			protected MessageDigest initialValue() {
				try {
					return MessageDigest.getInstance("SHA-1");
				} catch (NoSuchAlgorithmException e) {
					throw new GdxRuntimeException(e);
				}
			}
		};

		ThreadLocal<SHA1> currentHash = new ThreadLocal<SHA1>() {
			@Override
			protected SHA1 initialValue() {
				return null;
			}
		};

		ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
			@Override
			protected StringBuilder initialValue() {
				return new StringBuilder(40);
			}
		};

		void makeCurrent(SHA1 currentHash) {
			this.currentHash.set(currentHash);
		}

		boolean isCurrent(SHA1 currentHash) {
			return this.currentHash.get() == currentHash;
		}
	}

	private static final Algorithm algorithm = new Algorithm();

}
