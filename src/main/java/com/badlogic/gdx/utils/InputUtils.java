package com.badlogic.gdx.utils;

public class InputUtils {

	/**
	 * Limits mouse cursor to the window client area.
	 * <p>
	 * Only implemented on Windows.
	 */
	public static void clipCursor(long left, long top, long right, long bottom) {
		nclipCursor(left, top, right, bottom);
	}

	public static void freeCursor() {
		nfreeCursor();
	}

	// @off

	/*JNI
		#ifdef WIN32
		#include <Windows.h>
		#endif
	*/

	private static native void nclipCursor(long left, long top, long right, long bottom); /*
		#ifdef WIN32
		RECT rect;
		rect.left = left;
		rect.top = top;
		rect.right = right;
		rect.bottom = bottom;
		ClipCursor(&rect);
		#endif
	*/

	private static native void nfreeCursor(); /*
		#ifdef WIN32
		ClipCursor(NULL);
		#endif
	*/

}
