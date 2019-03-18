package com.badlogic.gdx.json;

import com.badlogic.gdx.concurrent.ThreadLocalInstance;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility functions to (optionally) store floats and doubles using a IEEE-754 bit masks.
 * <p>
 * Reference: http://the-witness.net/news/2011/12/engine-tech-concurrent-world-editing/
 */
public class JsonFloatSerializer {

	private static final String fpRegEx = "(-?[0-9.]+(E[+-][0-9]+)?)";
	private static final Pattern purePattern = Pattern.compile("^" + fpRegEx + "$");
	private static final Pattern ieeePattern = Pattern.compile("^0x([a-fA-F0-9]+)\\|" + fpRegEx + "$");

	private static final ThreadLocal<StringBuilder> stringBuilder =
			new ThreadLocalInstance<>(() -> new StringBuilder(32));

	public static String encodeFloatBits(float value) {

		StringBuilder builder = stringBuilder.get();

		builder.setLength(0);
		builder.append("0x");
		builder.append(String.format("%08X", Float.floatToRawIntBits(value)));
		builder.append("|");
		builder.append(value);

		return builder.toString();
	}

	public static float decodeFloatBits(String value, float defaultValue) {

		if (value == null) {
			return defaultValue;
		}

		Matcher matcher = ieeePattern.matcher(value);

		if (matcher.matches()) {
			String group = matcher.group(1);
			return Float.intBitsToFloat(Integer.parseUnsignedInt(group, 16));
		} else {
			matcher = purePattern.matcher(value);
			if (matcher.matches()) {
				String group = matcher.group(1);
				return Float.parseFloat(group);
			}
		}

		throw new GdxRuntimeException("Error parsing float from string: " + value);
	}

	public static String encodeDoubleBits(double value) {

		StringBuilder builder = stringBuilder.get();

		builder.setLength(0);
		builder.append("0x");
		builder.append(String.format("%016X", Double.doubleToRawLongBits(value)));
		builder.append("|");
		builder.append(value);

		return builder.toString();
	}

	public static double decodeDoubleBits(String value, double defaultValue) {

		if (value == null) {
			return defaultValue;
		}

		Matcher matcher = ieeePattern.matcher(value);

		if (matcher.matches()) {
			String group = matcher.group(1);
			return Double.longBitsToDouble(Long.parseLong(group, 16));
		} else {
			matcher = purePattern.matcher(value);
			if (matcher.matches()) {
				String group = matcher.group(1);
				return Double.parseDouble(group);
			}
		}

		throw new GdxRuntimeException("Error parsing double from string: " + value);
	}

}
