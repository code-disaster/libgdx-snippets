package com.badlogic.gdx.profiler;

import com.badlogic.gdx.function.Consumer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.remotery.Remotery;
import org.lwjgl.util.remotery.RemoteryGL;

public class RemoteryLwjgl implements Profiler {

	private static class SampleCPU extends Sample {

		@Override
		public void end() {
			Remotery.rmt_EndCPUSample();
		}
	}

	private static class SampleOGL extends Sample {

		@Override
		public void end() {
			RemoteryGL.rmt_EndOpenGLSample();
		}
	}

	private static long globalInstance = 0L;
	private static final Sample defaultSampleCPU = new SampleCPU();
	private static final Sample defaultSampleOGL = new SampleOGL();

	public static void initialize() {

		try (MemoryStack stack = MemoryStack.stackPush()) {

			PointerBuffer pb = stack.callocPointer(1);
			Remotery.rmt_CreateGlobalInstance(pb);

			globalInstance = pb.get(0);

			RemoteryGL.rmt_BindOpenGL();

			Remotery.rmt_SetCurrentThreadName("Render");
		}
	}

	public static void shutdown() {

		if (globalInstance != 0L) {

			RemoteryGL.rmt_UnbindOpenGL();

			Remotery.rmt_DestroyGlobalInstance(globalInstance);
			globalInstance = 0L;
		}
	}

	@Override
	public Sample sampleCPU(String name, boolean aggregate) {
		int flags = aggregate ? Remotery.RMTSF_Aggregate : Remotery.RMTSF_None;
		Remotery.rmt_BeginCPUSample(name, flags, null);
		return defaultSampleCPU;
	}

	@Override
	public <T> void sampleCPU(String name, boolean aggregate, T context, Consumer<T> consumer) {

		int flags = aggregate ? Remotery.RMTSF_Aggregate : Remotery.RMTSF_None;
		Remotery.rmt_BeginCPUSample(name, flags, null);

		consumer.accept(context);

		Remotery.rmt_EndCPUSample();
	}

	@Override
	public Sample sampleOpenGL(String name) {
		RemoteryGL.rmt_BeginOpenGLSample(name, null);
		return defaultSampleOGL;
	}

	@Override
	public <T> void sampleOpenGL(String name, T context, Consumer<T> consumer) {

		RemoteryGL.rmt_BeginOpenGLSample(name, null);

		consumer.accept(context);

		RemoteryGL.rmt_EndOpenGLSample();
	}

	@Override
	public void setThreadName(CharSequence name) {
		Remotery.rmt_SetCurrentThreadName(name);
	}

}
