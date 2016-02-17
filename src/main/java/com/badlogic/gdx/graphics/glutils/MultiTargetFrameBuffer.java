package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.badlogic.gdx.Gdx.gl30;
import static com.badlogic.gdx.graphics.GL30.*;
import static com.badlogic.gdx.graphics.GL33Ext.GL_CLAMP_TO_BORDER;
import static com.badlogic.gdx.graphics.GL33Ext.GL_TEXTURE_BORDER_COLOR;

/**
 * An extension to {@link FrameBuffer} with multiple color attachments. Can be used as
 * multi-render-target in deferred rendering (G-buffer).
 * <p>
 * Uses alternate depth/stencil buffer formats to allow for GL_DEPTH24_STENCIL8.
 */
public class MultiTargetFrameBuffer extends GLFrameBuffer<Texture> {

	public enum Format {

		R32F(GL_R32F, GL_RED, GL_FLOAT),
		RG32F(GL_RG32F, GL_RG, GL_FLOAT),
		RGB32F(GL_RGB32F, GL_RGB, GL_FLOAT),

		R8(GL_R8, GL_RED, GL_UNSIGNED_BYTE),

		PixmapFormat(GL_NONE, GL_NONE, GL_NONE);

		private final int internal, format, type;

		Format(int internal, int format, int type) {
			this.internal = internal;
			this.format = format;
			this.type = type;
		}
	}

	private Texture[] colorTextures;

	private int depthBufferHandle;
	private int depthStencilBufferHandle;

	private static Format[] fbCreateFormats;

	private static IntBuffer attachmentIds;
	private static final FloatBuffer tmpColors = BufferUtils.newFloatBuffer(4);

	/**
	 * Creates a new MRT FrameBuffer with the given color buffer format and dimensions.
	 */
	public static MultiTargetFrameBuffer create(Format format, int numColorBuffers,
												int width, int height, boolean hasDepth, boolean hasStencil) {

		return create(format, null, numColorBuffers, width, height, hasDepth, hasStencil);
	}

	/**
	 * Creates a new MRT FrameBuffer with the given {@link Pixmap} format and dimensions.
	 */
	public static MultiTargetFrameBuffer create(Pixmap.Format pixmapFormat, int numColorBuffers,
												int width, int height, boolean hasDepth, boolean hasStencil) {

		return create(Format.PixmapFormat, pixmapFormat, numColorBuffers, width, height, hasDepth, hasStencil);
	}

	/**
	 * Creates a new MRT FrameBuffer with the given color buffer format and dimensions. If the format is
	 * {@link Format#PixmapFormat}, the pixmapFormat parameter is used, otherwise it is ignored.
	 */
	public static MultiTargetFrameBuffer create(Format format, Pixmap.Format pixmapFormat, int numColorBuffers,
												int width, int height, boolean hasDepth, boolean hasStencil) {

		fbCreateFormats = new Format[numColorBuffers];
		Pixmap.Format[] formats = new Pixmap.Format[numColorBuffers];

		for (int i = 0; i < numColorBuffers; i++) {
			fbCreateFormats[i] = format;
			formats[i] = format == Format.PixmapFormat ? pixmapFormat : null;
		}

		return new MultiTargetFrameBuffer(formats, width, height, hasDepth, hasStencil);
	}

	/**
	 * Creates a new MRT FrameBuffer with the given color buffer formats and dimensions.
	 *
	 * This function equals {@link MultiTargetFrameBuffer#create(Format, Pixmap.Format, int, int, int, boolean, boolean)}
	 * but individually describes the format for each color buffer.
	 */
	public static MultiTargetFrameBuffer create(Format[] formats, Pixmap.Format[] pixmapFormats,
												int width, int height, boolean hasDepth, boolean hasStencil) {

		fbCreateFormats = formats;
		return new MultiTargetFrameBuffer(pixmapFormats, width, height, hasDepth, hasStencil);
	}

	private MultiTargetFrameBuffer(Pixmap.Format[] pixmapFormats,
								   int width, int height, boolean hasDepth, boolean hasStencil) {

		super(pixmapFormats[0], width, height, false, false);
		build(pixmapFormats, hasDepth, hasStencil);
	}

	/**
	 * Completes the MRT FrameBuffer by attaching the additional color buffers, plus optional depth and stencil buffers.
	 * This is done after the initial creation in {@link GLFrameBuffer#build()}, so glCheckFramebufferStatus() is
	 * called again.
	 */
	private void build(Pixmap.Format[] formats, boolean hasDepth, boolean hasStencil) {

		bind();

		// create and attach additional color buffers

		colorTextures = new Texture[formats.length];
		colorTextures[0] = colorTexture;

		for (int i = 1; i < formats.length; i++) {
			colorTextures[i] = createColorTexture(i, formats);
			gl30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D,
					colorTextures[i].getTextureObjectHandle(), 0);
		}

		synchronized (MultiTargetFrameBuffer.class) {

			if (attachmentIds == null || formats.length > attachmentIds.capacity()) {
				attachmentIds = BufferUtils.newIntBuffer(formats.length);
				for (int i = 0; i < formats.length; i++) {
					attachmentIds.put(i, GL_COLOR_ATTACHMENT0 + i);
				}
			}

			gl30.glDrawBuffers(formats.length, attachmentIds);
		}

		// depth texture, or depth/stencil render target

		if (hasStencil) {

			depthStencilBufferHandle = gl30.glGenRenderbuffer();

			gl30.glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBufferHandle);
			gl30.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);

			gl30.glBindRenderbuffer(GL_RENDERBUFFER, 0);

			gl30.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,
					GL_RENDERBUFFER, depthStencilBufferHandle);

		} else if (hasDepth) {

			depthBufferHandle = gl30.glGenTexture();

			gl30.glBindTexture(GL_TEXTURE_2D, depthBufferHandle);
			gl30.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, width, height, 0,
					GL_DEPTH_COMPONENT, GL_FLOAT, null);

			gl30.glBindTexture(GL_TEXTURE_2D, 0);

			gl30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
					GL_TEXTURE_2D, depthBufferHandle, 0);
		}

		// check status again

		int result = gl30.glCheckFramebufferStatus(GL_FRAMEBUFFER);

		unbind();

		if (result != GL_FRAMEBUFFER_COMPLETE) {
			dispose();
			throw new IllegalStateException("frame buffer couldn't be constructed: error " + result);
		}
	}

	@Override
	protected Texture createColorTexture() {
		return createColorTexture(0, new Pixmap.Format[] { format });
	}

	private Texture createColorTexture(int index, Pixmap.Format[] formats) {
		Texture result;

		if (fbCreateFormats[index] == Format.PixmapFormat) {
			int glFormat = Pixmap.Format.toGlFormat(formats[index]);
			int glType = Pixmap.Format.toGlType(formats[index]);
			GLOnlyTextureData data = new GLOnlyTextureData(width, height, 0, glFormat, glFormat, glType);
			result = new Texture(data);
		} else {
			ColorBufferTextureData data = new ColorBufferTextureData(fbCreateFormats[index], width, height);
			result = new Texture(data);
		}

		result.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		result.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

		return result;
	}

	@Override
	protected void disposeColorTexture(Texture colorTexture) {
		for (Texture texture : colorTextures) {
			texture.dispose();
		}

		if (depthBufferHandle != 0) {
			gl30.glDeleteTexture(depthBufferHandle);
		}

		if (depthStencilBufferHandle != 0) {
			gl30.glDeleteRenderbuffer(depthStencilBufferHandle);
		}
	}

	public Texture getColorBufferTexture(int index) {
		return colorTextures[index];
	}

	public void clampToBorder(int index, Color color) {
		int handle = colorTextures[index].getTextureObjectHandle();
		gl30.glBindTexture(GL_TEXTURE_2D, handle);

		gl30.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		gl30.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

		synchronized (tmpColors) {
			tmpColors.clear();
			tmpColors.put(color.r);
			tmpColors.put(color.g);
			tmpColors.put(color.b);
			tmpColors.put(color.a);
			tmpColors.flip();

			gl30.glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, tmpColors);
		}

		gl30.glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void clearColorBuffer(Color color, int index) {
		synchronized (tmpColors) {
			tmpColors.clear();
			tmpColors.put(color.r);
			tmpColors.put(color.g);
			tmpColors.put(color.b);
			tmpColors.put(color.a);
			tmpColors.flip();

			gl30.glClearBufferfv(GL_COLOR, index, tmpColors);
		}
	}

	public void clearColorBuffers(Color color) {
		synchronized (tmpColors) {
			tmpColors.clear();
			tmpColors.put(color.r);
			tmpColors.put(color.g);
			tmpColors.put(color.b);
			tmpColors.put(color.a);
			tmpColors.flip();

			for (int index = 0; index < colorTextures.length; index++) {
				gl30.glClearBufferfv(GL_COLOR, index, tmpColors);
			}
		}
	}

	public void clearColorBuffers(Color color, int[] indices) {
		synchronized (tmpColors) {
			tmpColors.clear();
			tmpColors.put(color.r);
			tmpColors.put(color.g);
			tmpColors.put(color.b);
			tmpColors.put(color.a);
			tmpColors.flip();

			for (int index : indices) {
				gl30.glClearBufferfv(GL_COLOR, index, tmpColors);
			}
		}
	}

	public void clearDepthBuffer(float depth) {
		synchronized (tmpColors) {
			tmpColors.clear();
			tmpColors.put(depth);
			tmpColors.flip();

			gl30.glClearBufferfv(GL_DEPTH, 0, tmpColors);
		}
	}

	public void clearDepthStencilBuffer(float depth, int stencil) {
		gl30.glClearBufferfi(GL_DEPTH_STENCIL, 0, depth, stencil);
	}

	private static class ColorBufferTextureData implements TextureData {

		private final Format format;
		private final int width;
		private final int height;

		ColorBufferTextureData(Format format, int width, int height) {
			this.format = format;
			this.width = width;
			this.height = height;
		}

		@Override
		public TextureDataType getType() {
			return TextureDataType.Custom;
		}

		@Override
		public boolean isPrepared() {
			return true;
		}

		@Override
		public void prepare() {
		}

		@Override
		public Pixmap consumePixmap() {
			return null;
		}

		@Override
		public boolean disposePixmap() {
			return false;
		}

		@Override
		public void consumeCustomData(int target) {
			gl30.glTexImage2D(target, 0, format.internal, width, height, 0, format.format, format.type, null);
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public Pixmap.Format getFormat() {
			return null;
		}

		@Override
		public boolean useMipMaps() {
			return false;
		}

		@Override
		public boolean isManaged() {
			return true;
		}
	}
}
