package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.utils.*;

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
public class MultiTargetFrameBuffer implements Disposable {

	public enum Format {

		R32F(GL_R32F, GL_RED, GL_FLOAT),
		RG32F(GL_RG32F, GL_RG, GL_FLOAT),
		RGB32F(GL_RGB32F, GL_RGB, GL_FLOAT),
		RGBA32F(GL_RGBA32F, GL_RGBA, GL_FLOAT),

		RG16F(GL_RG16F, GL_RG, GL_HALF_FLOAT),

		R8(GL_R8, GL_RED, GL_UNSIGNED_BYTE),
		RG8(GL_RG8, GL_RG, GL_UNSIGNED_BYTE),

		R16I(GL_R16I, GL_RED_INTEGER, GL_SHORT),
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

	private final FrameBuffer frameBuffer;
	private final Texture[] colorTextures;
	private final boolean hasStencil;

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

		ColorAttachmentFormat[] fbCreateFormats = new ColorAttachmentFormat[numColorBuffers];

		for (int i = 0; i < numColorBuffers; i++) {
			fbCreateFormats[i] = new ColorAttachmentFormat(format, pixmapFormat);
		}

		return create(fbCreateFormats, width, height, hasDepth, hasStencil);
	}

	/**
	 * Creates a new MRT FrameBuffer with the given color buffer formats and dimensions.
	 * <p>
	 * This function equals {@link MultiTargetFrameBuffer#create(Format, Pixmap.Format, int, int, int, boolean, boolean)}
	 * but individually describes the format for each color buffer.
	 */
	public static MultiTargetFrameBuffer create(ColorAttachmentFormat[] formats,
												int width,
												int height,
												boolean hasDepth,
												boolean hasStencil) {

		FrameBufferBuilder builder = new FrameBufferBuilder(width, height);

		for (ColorAttachmentFormat attachment : formats) {

			if (attachment.format == Format.PixmapFormat) {
				builder.addBasicColorTextureAttachment(attachment.pixmapFormat);
			} else {
				builder.addColorTextureAttachment(attachment.format.internal,
						attachment.format.format, attachment.format.type);
			}
		}

		if (hasDepth && hasStencil) {
			builder.addBasicStencilDepthPackedRenderBuffer();
		} else if (hasDepth) {
			builder.addDepthTextureAttachment(GL_DEPTH_COMPONENT32F, GL_FLOAT);
		}

		FrameBuffer frameBuffer = builder.build();
		Array<Texture> textures = frameBuffer.getTextureAttachments();

		for (int i = 0; i < formats.length; i++) {

			ColorAttachmentFormat attachment = formats[i];
			Texture texture = textures.get(i);

			texture.setFilter(attachment.minFilter, attachment.magFilter);
			texture.setWrap(attachment.wrap, attachment.wrap);
		}

		return new MultiTargetFrameBuffer(frameBuffer, hasDepth, hasStencil);
	}

	private MultiTargetFrameBuffer(FrameBuffer frameBuffer, boolean hasDepth, boolean hasStencil) {

		this.frameBuffer = frameBuffer;

		this.colorTextures = new Texture[frameBuffer.textureAttachments.size];
		for (int i = 0; i < frameBuffer.textureAttachments.size; i++) {
			this.colorTextures[i] = frameBuffer.textureAttachments.get(i);
		}

		this.hasStencil = hasStencil;
	}

	@Override
	public void dispose() {
		frameBuffer.dispose();
	}

	public int getWidth() {
		return frameBuffer.getWidth();
	}

	public int getHeight() {
		return frameBuffer.getHeight();
	}

	public void bind() {
		frameBuffer.bind();
	}

	public void begin() {
		frameBuffer.begin();
	}

	public void end() {
		frameBuffer.end();
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

	public static void readColorBuffer(Pixmap target,
									   MultiTargetFrameBuffer source, int srcIndex,
									   int srcX0, int srcY0, int srcX1, int srcY1) {

		gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, source.frameBuffer.getFramebufferHandle());
		gl30.glReadBuffer(GL_COLOR_ATTACHMENT0 + srcIndex);

		gl30.glPixelStorei(GL_PACK_ALIGNMENT, 1);

		ByteBuffer pixels = target.getPixels();

		int glFormat = target.getGLFormat();
		gl30.glReadPixels(srcX0, srcY0, srcX1 - srcX0, srcY1 - srcY0, glFormat, GL_UNSIGNED_BYTE, pixels);

		gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		gl30.glReadBuffer(GL_BACK);
	}

	public static void readFloatBuffer(FloatBuffer target,
									   MultiTargetFrameBuffer source, int srcIndex,
									   int srcX0, int srcY0, int srcX1, int srcY1) {

		gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, source.frameBuffer.getFramebufferHandle());
		gl30.glReadBuffer(GL_COLOR_ATTACHMENT0 + srcIndex);

		gl30.glPixelStorei(GL_PACK_ALIGNMENT, 4);

		gl30.glReadPixels(srcX0, srcY0, srcX1 - srcX0, srcY1 - srcY0, GL_RED, GL_FLOAT, target);

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

		int sourceFbo = source.frameBuffer.getFramebufferHandle();
		int targetFbo = target.frameBuffer.getFramebufferHandle();

		gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, sourceFbo);
		gl30.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, targetFbo);

		gl30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, destX0, destY0, destX1, destY1, mask, GL_NEAREST);

		gl30.glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		gl30.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}

}
