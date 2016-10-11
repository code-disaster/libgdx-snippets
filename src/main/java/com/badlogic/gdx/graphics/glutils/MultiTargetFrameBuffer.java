package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.*;

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
		RGBA32F(GL_RGBA32F, GL_RGBA, GL_FLOAT),

		RG16F(GL_RG16F, GL_RG, GL_FLOAT),

		R8(GL_R8, GL_RED, GL_UNSIGNED_BYTE),
		RG8(GL_RG8, GL_RG, GL_UNSIGNED_BYTE),

		R32I(GL_R32I, GL_RED_INTEGER, GL_INT),

		PixmapFormat(GL_NONE, GL_NONE, GL_NONE);

		private final int internal, format, type;

		Format(int internal, int format, int type) {
			this.internal = internal;
			this.format = format;
			this.type = type;
		}
	}

	public static class ColorAttachmentFormat {

		Format format = Format.PixmapFormat;
		Pixmap.Format pixmapFormat = Pixmap.Format.RGB888;
		boolean generateMipmaps = false;
		Texture.TextureFilter minFilter = Texture.TextureFilter.Nearest;
		Texture.TextureFilter magFilter = Texture.TextureFilter.Nearest;
		Texture.TextureWrap wrap = Texture.TextureWrap.ClampToEdge;

		public ColorAttachmentFormat(Format format,
									 Pixmap.Format pixmapFormat) {
			this.format = format;
			this.pixmapFormat = pixmapFormat;
		}

		public ColorAttachmentFormat(Format format,
									 Pixmap.Format pixmapFormat,
									 boolean generateMipmaps,
									 Texture.TextureFilter minFilter,
									 Texture.TextureFilter magFilter) {
			this.format = format;
			this.pixmapFormat = pixmapFormat;
			this.generateMipmaps = generateMipmaps;
			this.minFilter = minFilter;
			this.magFilter = magFilter;
		}

		public ColorAttachmentFormat(Format format,
									 Pixmap.Format pixmapFormat,
									 boolean generateMipmaps,
									 Texture.TextureFilter minFilter,
									 Texture.TextureFilter magFilter,
									 Texture.TextureWrap wrap) {
			this.format = format;
			this.pixmapFormat = pixmapFormat;
			this.generateMipmaps = generateMipmaps;
			this.minFilter = minFilter;
			this.magFilter = magFilter;
			this.wrap = wrap;
		}
	}

	private Texture[] colorTextures;

	private int depthBufferHandle;
	private int depthStencilBufferHandle;

	private static ColorAttachmentFormat[] fbCreateFormats;

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

		fbCreateFormats = new ColorAttachmentFormat[numColorBuffers];

		for (int i = 0; i < numColorBuffers; i++) {
			fbCreateFormats[i] = new ColorAttachmentFormat(format, pixmapFormat);
		}

		return new MultiTargetFrameBuffer(width, height, hasDepth, hasStencil);
	}

	/**
	 * Creates a new MRT FrameBuffer with the given color buffer formats and dimensions.
	 *
	 * This function equals {@link MultiTargetFrameBuffer#create(Format, Pixmap.Format, int, int, int, boolean, boolean)}
	 * but individually describes the format for each color buffer.
	 */
	public static MultiTargetFrameBuffer create(ColorAttachmentFormat[] formats,
												int width, int height,
												boolean hasDepth, boolean hasStencil) {

		fbCreateFormats = formats;
		return new MultiTargetFrameBuffer(width, height, hasDepth, hasStencil);
	}

	private MultiTargetFrameBuffer(int width, int height, boolean hasDepth, boolean hasStencil) {

		super(Pixmap.Format.RGB888, width, height, false, false);
		build(hasDepth, hasStencil);
	}

	/**
	 * Completes the MRT FrameBuffer by attaching the additional color buffers, plus optional depth and stencil buffers.
	 * This is done after the initial creation in {@link GLFrameBuffer#build()}, so glCheckFramebufferStatus() is
	 * called again.
	 */
	private void build(boolean hasDepth, boolean hasStencil) {

		bind();

		// create and attach additional color buffers

		int numColorAttachments = fbCreateFormats.length;

		colorTextures = new Texture[numColorAttachments];
		colorTextures[0] = colorTexture;

		for (int i = 1; i < numColorAttachments; i++) {
			colorTextures[i] = createColorTexture(i);
			gl30.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D,
					colorTextures[i].getTextureObjectHandle(), 0);
		}

		synchronized (MultiTargetFrameBuffer.class) {

			if (attachmentIds == null || numColorAttachments > attachmentIds.capacity()) {
				attachmentIds = BufferUtils.newIntBuffer(numColorAttachments);
				for (int i = 0; i < numColorAttachments; i++) {
					attachmentIds.put(i, GL_COLOR_ATTACHMENT0 + i);
				}
			}

			gl30.glDrawBuffers(numColorAttachments, attachmentIds);
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
		return createColorTexture(0);
	}

	private Texture createColorTexture(int index) {
		Texture result;

		ColorAttachmentFormat format = fbCreateFormats[index];

		if (format.format == Format.PixmapFormat) {
			int glFormat = Pixmap.Format.toGlFormat(format.pixmapFormat);
			int glType = Pixmap.Format.toGlType(format.pixmapFormat);
			GLOnlyTextureData data = new GLOnlyTextureData(width, height, 0, glFormat, glFormat, glType);
			result = new Texture(data);
		} else {
			ColorBufferTextureData data = new ColorBufferTextureData(format.format, format.generateMipmaps, width, height);
			result = new Texture(data);
		}

		result.setFilter(format.minFilter, format.magFilter);
		result.setWrap(format.wrap, format.wrap);

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

	public void generateMipmap(int index) {
		int handle = colorTextures[index].getTextureObjectHandle();
		gl30.glBindTexture(GL_TEXTURE_2D, handle);
		gl30.glGenerateMipmap(GL_TEXTURE_2D);
		gl30.glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void clearColorBuffer(Color color, int index) {
		clearColorBuffer(color.r, color.g, color.b, color.a, index);
	}

	public void clearColorBuffer(float r, float g, float b, float a, int index) {
		synchronized (tmpColors) {
			tmpColors.clear();
			tmpColors.put(r);
			tmpColors.put(g);
			tmpColors.put(b);
			tmpColors.put(a);
			tmpColors.flip();

			gl30.glClearBufferfv(GL_COLOR, index, tmpColors);
		}
	}

	public void clearColorBuffers(Color color) {
		clearColorBuffers(color.r, color.g, color.b, color.a);
	}

	public void clearColorBuffers(float r, float g, float b, float a) {
		synchronized (tmpColors) {
			tmpColors.clear();
			tmpColors.put(r);
			tmpColors.put(g);
			tmpColors.put(b);
			tmpColors.put(a);
			tmpColors.flip();

			for (int index = 0; index < colorTextures.length; index++) {
				gl30.glClearBufferfv(GL_COLOR, index, tmpColors);
			}
		}
	}

	public void clearColorBuffers(Color color, int[] indices) {
		clearColorBuffers(color.r, color.g, color.b, color.a, indices);
	}

	public void clearColorBuffers(float r, float g, float b, float a, int[] indices) {
		synchronized (tmpColors) {
			tmpColors.clear();
			tmpColors.put(r);
			tmpColors.put(g);
			tmpColors.put(b);
			tmpColors.put(a);
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
		private final boolean generateMipmap;

		private boolean isPrepared = false;

		ColorBufferTextureData(Format format, boolean generateMipmap, int width, int height) {
			this.format = format;
			this.generateMipmap = generateMipmap;
			this.width = width;
			this.height = height;
		}

		@Override
		public TextureDataType getType() {
			return TextureDataType.Custom;
		}

		@Override
		public boolean isPrepared() {
			return isPrepared;
		}

		@Override
		public void prepare() {
			isPrepared = true;
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
			if (generateMipmap) {
				gl30.glGenerateMipmap(target);
			}
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
			return generateMipmap;
		}

		@Override
		public boolean isManaged() {
			return true;
		}
	}

	public static void readColorBuffer(Pixmap target,
									   MultiTargetFrameBuffer source, int srcIndex,
									   int srcX0, int srcY0, int srcX1, int srcY1) {

		gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, source.getFramebufferHandle());
		gl30.glReadBuffer(GL_COLOR_ATTACHMENT0 + srcIndex);

		gl30.glPixelStorei(GL_PACK_ALIGNMENT, 1);

		ByteBuffer pixels = target.getPixels();
		gl30.glReadPixels(srcX0, srcY0, srcX1 - srcX0, srcY1 - srcY0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

		gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		gl30.glReadBuffer(GL_BACK);
	}

	public static void copyDepthStencilBuffer(MultiTargetFrameBuffer target,
											  int destX0, int destY0, int destX1, int destY1,
											  MultiTargetFrameBuffer source,
											  int srcX0, int srcY0, int srcX1, int srcY1) {

		int mask = GL_DEPTH_BUFFER_BIT;

		if (source.hasStencil && target.hasStencil) {
			mask |= GL_STENCIL_BUFFER_BIT;
		}

		int sourceFbo = source.getFramebufferHandle();
		int targetFbo = target.getFramebufferHandle();

		gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, sourceFbo);
		gl30.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, targetFbo);

		gl30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, destX0, destY0, destX1, destY1, mask, GL_NEAREST);

		gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		gl30.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}

}
