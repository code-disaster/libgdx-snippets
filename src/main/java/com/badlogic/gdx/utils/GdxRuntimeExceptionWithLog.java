package com.badlogic.gdx.utils;

import org.slf4j.helpers.MessageFormatter;

/**
 * Variation of {@link GdxRuntimeException} which additionally writes to an user-specified
 * {@link org.slf4j.Logger} instance.
 */
public class GdxRuntimeExceptionWithLog extends RuntimeException {

	public GdxRuntimeExceptionWithLog(org.slf4j.Logger log, String message) {
		super(message);
		log.error(message);
	}

	public GdxRuntimeExceptionWithLog(org.slf4j.Logger log, Throwable t) {
		super(t);
		log.error("", t);
	}

	public GdxRuntimeExceptionWithLog(org.slf4j.Logger log, String message, Throwable t) {
		super(message, t);
		log.error(message, t);
	}

	public GdxRuntimeExceptionWithLog(org.slf4j.Logger log, String format, Object... arguments) {
		super(formatMessage(format, arguments));
		log.error(format, arguments);
	}

	private static String formatMessage(String format, Object[] arguments) {
		return MessageFormatter.arrayFormat(format, arguments).getMessage();
	}

}
