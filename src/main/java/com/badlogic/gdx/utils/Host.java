package com.badlogic.gdx.utils;

public class Host {

	public enum OS {
		Linux,
		MacOS,
		Windows,
		Unknown
	}

	public enum OSVersion {
		Windows7,
		Unspecified
	}

	/**
	 * Stores type of the host operating system.
	 */
	public static final OS os = getHostOS();

	/**
	 * I know this is a terrible idea, but we use this identifier for some workarounds
	 * specific to Windows 7.
	 */
	public static final OSVersion version = getHostOSVersion();

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

	private static OSVersion getHostOSVersion() {

		OSVersion version = OSVersion.Unspecified;
		String osName = System.getProperty("os.name");

		if (osName.contains("Windows 7")) {
			version = OSVersion.Windows7;
		}

		return version;
	}

}
