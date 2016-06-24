package com.badlogic.gdx.lang;

public final class FourCC {

	private int value;

	public FourCC(CharSequence name) {
		this(convert(name));
	}

	public FourCC(char c0, char c1, char c2, char c3) {
		this(convert(c0, c1, c2, c3));
	}

	public FourCC(int value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FourCC) {
			return value == ((FourCC) obj).value;
		}
		return false;
	}

	@Override
	public String toString() {
		return Character.toString((char) ((value >>> 24) & 0xff))
				+ Character.toString((char) ((value >>> 16) & 0xff))
				+ Character.toString((char) ((value >>> 8) & 0xff))
				+ Character.toString((char) (value & 0xff));
	}

	private static int convert(CharSequence name) {
		return convert(name.charAt(0), name.charAt(1), name.charAt(2), name.charAt(3));
	}

	private static int convert(char c0, char c1, char c2, char c3) {
		return (c0 << 24) | (c1 << 16) | (c2 << 8) | c3;
	}

}
