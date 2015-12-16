package com.badlogic.gdx.profiler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JNI wrapper to Remotery (https://github.com/Celtoys/Remotery), a realtime CPU/OpenGL profiler with
 * web browser viewer.
 */
public class Remotery implements Profiler {

	private static boolean initialized = false;

	private static Map<String, Long> threadNames = new ConcurrentHashMap<>();
	private static Map<String, Sample> cacheCPUSamples = new ConcurrentHashMap<>();
	private static Map<String, Sample> cacheOpenGLSamples = new ConcurrentHashMap<>();

	private static class Sample {
		public long name;
		public int hash;

		public Sample() {}

		public Sample(String name, int hash) {
			this.name = strdupNativeName(name);
			this.hash = hash;
		}
	}

	private static Sample defaultSample = new Sample();

	public static class Settings {
		public int port = 0x4597;
		public int msSleepBetweenServerUpdates = 10;
		public int messageQueueSizeInBytes = 64 * 1024;
		public int maxNbMessagesPerUpdate = 100;
	}

	public static boolean createGlobalInstance(Settings settings) {

		if (!initialized) {

			if (settings != null) {
				rmtChangeSettings(settings.port, settings.msSleepBetweenServerUpdates,
						settings.messageQueueSizeInBytes, settings.maxNbMessagesPerUpdate);
			}

			int error = rmtCreateGlobalInstance();
			if (error != 0) {
				return false;
			}

			initialized = true;
		}

		return true;
	}

	public static void destroyGlobalInstance() {

		if (initialized) {

			rmtDestroyGlobalInstance();

			threadNames.values().forEach(Remotery::freeNativeName);
			cacheCPUSamples.values().forEach(sample -> freeNativeName(sample.name));
			cacheOpenGLSamples.values().forEach(sample -> freeNativeName(sample.name));

			initialized = false;
		}
	}

	public static void setCurrentThreadName(String name) {

		long pointer = threadNames.getOrDefault(name, 0L);

		if (pointer == 0) {
			pointer = strdupNativeName(name);
			threadNames.put(name, pointer);
		}

		if (pointer != 0) {
			rtmSetCurrentThreadName(pointer);
		}
	}

	public static void beginCPUSample(String name) {

		Sample sample = cacheCPUSamples.getOrDefault(name, defaultSample);

		if (sample.name == 0) {
			sample = new Sample(name, 0);
			cacheCPUSamples.put(name, sample);
		}

		int hash = rmtBeginCPUSample(sample.name, sample.hash);

		if (sample.hash == 0) {
			sample.hash = hash;
		}
	}

	@Override
	public void sampleCPU(String name, Runnable runnable) {

		if (initialized) {
			beginCPUSample(name);
		}

		runnable.run();

		if (initialized) {
			endCPUSample();
		}
	}

	public static void beginOpenGLSample(String name) {

		Sample sample = cacheOpenGLSamples.getOrDefault(name, defaultSample);

		if (sample.name == 0) {
			sample = new Sample(name, 0);
			cacheOpenGLSamples.put(name, sample);
		}

		int hash = rmtBeginOpenGLSample(sample.name, sample.hash);

		if (sample.hash == 0) {
			sample.hash = hash;
		}
	}

	@Override
	public void sampleOpenGL(String name, Runnable runnable) {

		if (initialized) {
			beginOpenGLSample(name);
		}

		runnable.run();

		if (initialized) {
			endOpenGLSample();
		}
	}

	// @off

	/*JNI
		#include <stdlib.h>
		#include <string.h>
		#include "Remotery.h"

		static Remotery* remotery = NULL;
	*/

	private static native void rmtChangeSettings(int port, int msSleepBetweenServerUpdates,
												 int messageQueueSizeInBytes, int maxNbMessagesPerUpdate); /*

		rmtSettings* settings = _rmt_Settings();
		settings->port = port;
		settings->msSleepBetweenServerUpdates = msSleepBetweenServerUpdates;
		settings->messageQueueSizeInBytes = messageQueueSizeInBytes;
		settings->maxNbMessagesPerUpdate = maxNbMessagesPerUpdate;
	*/

	private static native int rmtCreateGlobalInstance(); /*
		rmtError result = rmt_CreateGlobalInstance(&remotery);
		return (int) result;
	*/

	private static native void rmtDestroyGlobalInstance(); /*
		rmt_DestroyGlobalInstance(remotery);
		remotery = NULL;
	*/

	private static native void rtmSetCurrentThreadName(long pointer); /*
		rmt_SetCurrentThreadName((const char*) pointer);
	*/

	public static native void logText(String text); /*
		rmt_LogText(text);
	*/

	public static native int rmtBeginCPUSample(long name, int hash); /*
		rmtU32 hash_cache = hash & 0xffffffff;
		_rmt_BeginCPUSample((const char*) name, &hash_cache);
		return hash_cache;
	*/

	public static native void endCPUSample(); /*
		rmt_EndCPUSample();
	*/

	public static native void bindOpenGL(); /*
		rmt_BindOpenGL();
	*/

	public static native void unbindOpenGL(); /*
		rmt_UnbindOpenGL();
	*/

	public static native int rmtBeginOpenGLSample(long name, int hash); /*
		rmtU32 hash_cache = hash & 0xffffffff;
		#if RMT_USE_OPENGL
		_rmt_BeginOpenGLSample((const char*) name, &hash_cache);
		#endif
		return hash_cache;
	*/

	public static native void endOpenGLSample(); /*
		#if RMT_USE_OPENGL
		rmt_EndOpenGLSample();
		#endif
	*/

	private static native long strdupNativeName(String name); /*
		return (long) strdup(name);
	*/

	private static native void freeNativeName(long pointer); /*
		free((char*) pointer);
	*/
}
