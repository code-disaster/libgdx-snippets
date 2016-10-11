package com.badlogic.gdx.profiler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * JNI wrapper to Remotery (https://github.com/Celtoys/Remotery), a realtime CPU/OpenGL profiler with
 * web browser viewer.
 */
public class Remotery implements Profiler {

	private static boolean initialized = false;
	private static boolean initializedOpenGL = false;

	private static Map<String, Long> threadNames = new ConcurrentHashMap<>();
	private static Map<String, CPUSample> cacheCPUSamples = new ConcurrentHashMap<>();
	private static Map<String, OpenGLSample> cacheOpenGLSamples = new ConcurrentHashMap<>();

	private abstract static class Sample implements AutoCloseable {
		long name;
		int hash;

		Sample(String name, int hash) {
			this.name = name != null ? strdupNativeName(name) : 0L;
			this.hash = hash;
		}
	}

	public static class CPUSample extends Sample {

		CPUSample(String name, int hash) {
			super(name, hash);
		}

		@Override
		public void close() throws Exception {
			endCPUSample();
		}
	}

	public static class OpenGLSample extends Sample {

		OpenGLSample(String name, int hash) {
			super(name, hash);
		}

		@Override
		public void close() throws Exception {
			endOpenGLSample();
		}
	}

	private static CPUSample defaultCPUSample = new CPUSample(null, 0);
	private static OpenGLSample defaultOpenGLSample = new OpenGLSample(null, 0);

	public static class Settings {
		public int port = 0x4597;
		public int msSleepBetweenServerUpdates = 10;
		public int messageQueueSizeInBytes = 64 * 1024;
		public int maxNbMessagesPerUpdate = 100;
	}

	public static class SampleFlags {
		public static final int None = 0;
		public static final int Aggregate = 1;
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
			rmtSetCurrentThreadName(pointer);
		}
	}

	public static CPUSample beginCPUSample(String name, boolean aggregate) {

		CPUSample sample = cacheCPUSamples.getOrDefault(name, defaultCPUSample);

		if (sample.name == 0) {
			sample = new CPUSample(name, 0);
			cacheCPUSamples.put(name, sample);
		}

		int flags = aggregate ? SampleFlags.Aggregate : SampleFlags.None;

		int hash = rmtBeginCPUSample(sample.name, flags, sample.hash);

		if (sample.hash == 0) {
			sample.hash = hash;
		}

		return sample;
	}

	@Override
	public AutoCloseable sampleCPU(String name, boolean aggregate) {
		return beginCPUSample(name, aggregate);
	}

	@Override
	public <T> void sampleCPU(String name, boolean aggregate, T context, Consumer<T> consumer) {

		if (initialized) {
			beginCPUSample(name, aggregate);
		}

		consumer.accept(context);

		if (initialized) {
			endCPUSample();
		}
	}

	public static void bindOpenGL() {

		if (initialized && !initializedOpenGL) {
			rmtBindOpenGL();
			initializedOpenGL = true;
		}
	}

	public static void unbindOpenGL() {

		if (initializedOpenGL) {
			rmtUnbindOpenGL();
			initializedOpenGL = false;
		}
	}

	public static OpenGLSample beginOpenGLSample(String name) {

		OpenGLSample sample = cacheOpenGLSamples.getOrDefault(name, defaultOpenGLSample);

		if (sample.name == 0) {
			sample = new OpenGLSample(name, 0);
			cacheOpenGLSamples.put(name, sample);
		}

		int hash = rmtBeginOpenGLSample(sample.name, sample.hash);

		if (sample.hash == 0) {
			sample.hash = hash;
		}

		return sample;
	}

	@Override
	public AutoCloseable sampleOpenGL(String name) {
		return beginOpenGLSample(name);
	}

	@Override
	public <T> void sampleOpenGL(String name, T context, Consumer<T> consumer) {

		if (initializedOpenGL) {
			beginOpenGLSample(name);
		}

		consumer.accept(context);

		if (initializedOpenGL) {
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

	private static native void rmtSetCurrentThreadName(long pointer); /*
		rmt_SetCurrentThreadName((const char*) pointer);
	*/

	public static native void logText(String text); /*
		rmt_LogText(text);
	*/

	public static native int rmtBeginCPUSample(long name, int flags, int hash); /*
		rmtU32 hash_cache = hash & 0xffffffff;
		_rmt_BeginCPUSample((const char*) name, flags, &hash_cache);
		return hash_cache;
	*/

	public static native void endCPUSample(); /*
		rmt_EndCPUSample();
	*/

	public static native void rmtBindOpenGL(); /*
		rmt_BindOpenGL();
	*/

	public static native void rmtUnbindOpenGL(); /*
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
		return (int64_t) strdup(name);
	*/

	private static native void freeNativeName(long pointer); /*
		free((char*) pointer);
	*/
}
