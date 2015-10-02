package com.badlogic.gdx.utils;

public class Host {

	public enum OS {
		Linux,
		MacOS,
		Windows,
		Unknown
	}

	/**
	 * Stores type of the host operating system.
	 */
	public static final OS os = getHostOS();

	private static OS getHostOS() {

		OS os = OS.Unknown;
		String osName = System.getProperty("os.name");

		if (osName.contains("Linux")) {
			os = OS.Linux;
		} else if (osName.contains("Mac")) {
			os = OS.MacOS;
		} else if (osName.contains("Windows")) {
			os = OS.Windows;
		}

		return os;
	}

}
