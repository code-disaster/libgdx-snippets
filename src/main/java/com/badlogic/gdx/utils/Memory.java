package com.badlogic.gdx.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Memory utility functions.
 *
 * Uses sun.misc.Unsafe.
 */
public class Memory {

	private static final Unsafe theUnsafe;
	private static final long addressFieldOffset;

	/**
	 * Allocates a direct {@link ByteBuffer} with an user-defined alignment.
	 *
	 * Internally a slightly larger buffer is created, wasting up to [alignment] bytes of native memory.
	 */
	public static ByteBuffer allocateDirectAligned(int capacity, long alignment) {

		// allocate a direct buffer with padding
		ByteBuffer buffer = ByteBuffer.allocateDirect((int) (capacity + alignment));

		// use Unsafe access to obtain memory address of buffer
		long address = getAddress(buffer);

		if ((address & (alignment - 1)) == 0) {
			// buffer is aligned already
			buffer.limit(capacity);
		} else {
			// realign buffer
			int position = (int) (alignment - (address & (alignment - 1)));
			buffer.position(position);
			buffer.limit(position + capacity);
		}

		return buffer.slice().order(ByteOrder.nativeOrder());
	}

	/**
	 * Uses the native memset() function to clear parts of a direct {@link ByteBuffer}.
	 *
	 * Does neither observe nor modify {@link ByteBuffer#position()} and {@link ByteBuffer#limit()}, which means
	 * the state of the ByteBuffer object is not changed by this function.
	 */
	public static void memsetDirect(ByteBuffer buffer, byte value, int offset, int count) {
		if (!buffer.isDirect()) {
			throw new GdxRuntimeException("Not a direct buffer!");
		}

		if (buffer.capacity() < offset + count) {
			throw new GdxRuntimeException("Buffer overflow!");
		}

		long address = getAddress(buffer);
		memset(address, value, offset, count);
	}

	public static void memsetDirect(long address, byte value, int offset, int count) {
		memset(address, value, offset, count);
	}

	public static long getAddress(ByteBuffer buffer) {
		return theUnsafe.getLong(buffer, addressFieldOffset);
	}

	private static Unsafe getUnsafe() {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			return (Unsafe)f.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static {
		try {
			theUnsafe = getUnsafe();
			addressFieldOffset = theUnsafe.objectFieldOffset(Buffer.class.getDeclaredField("address"));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	// @off

	/*JNI
		#include <memory.h>
	*/

	private static native void memset(long address, int value, int offset, int count); /*
		unsigned char* ptr = (unsigned char*) address + offset;
		memset(ptr, value, count);
	*/

}
