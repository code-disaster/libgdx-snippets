package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.nio.ByteBuffer;

/**
 * Defines a rectangular area of a {@link Pixmap}.
 */
public class PixmapRegion {

	private Pixmap pixmap;
	private ByteBuffer pixels;
	private int x, y;
	private int width, height;
	private int pixelStride, lineStride;

	public PixmapRegion() {

	}

	public PixmapRegion(Pixmap pixmap) {
		this(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight());
	}

	public PixmapRegion(PixmapRegion region) {
		setRegion(region);
	}

	public PixmapRegion(PixmapRegion region, int x, int y, int width, int height) {
		this(region.pixmap, region.x + x, region.y + y, width, height);
	}

	public PixmapRegion(Pixmap pixmap, int x, int y, int width, int height) {
		setRegion(pixmap, x, y, width, height);
	}

	public PixmapRegion(Pixmap pixmap, PixmapRegion region) {
		setRegion(pixmap, region.x, region.y, region.width, region.height);
	}

	private void changePixmap(Pixmap pixmap) {
		this.pixmap = pixmap;
		pixels = pixmap.getPixels();
		pixelStride = PixmapUtils.getPixelStride(pixmap.getFormat());
		lineStride = pixmap.getWidth() * pixelStride;
	}

	public Pixmap getPixmap() {
		return pixmap;
	}

	public int getPixel(int x, int y) {
		if (pixelStride != 4) {
			throw new GdxRuntimeException("Unsupported format");
		}
		if (x < 0 || x >= width || y < 0 || y >= height) {
			throw new GdxRuntimeException("Out of bounds");
		}
		int offset = (this.y + y) * lineStride + (this.x + x) * pixelStride;
		return pixels.getInt(offset);
	}

	public int getPixel(int x, int y, boolean flipX, boolean flipY) {
		return getPixel(
				flipX ? width - 1 - x : x,
				flipY ? height - 1 - y : y);
	}

	public int getPixelSlow(int x, int y) {
		if (pixelStride < 3) {
			throw new GdxRuntimeException("Unsupported format");
		}

		int offset = (this.y + y) * lineStride + (this.x + x) * pixelStride;
		int r = Byte.toUnsignedInt(pixels.get(offset));
		int g = Byte.toUnsignedInt(pixels.get(offset + 1));
		int b = Byte.toUnsignedInt(pixels.get(offset + 2));

		int a;
		if (pixelStride < 4) {
			a = (r + g + b) >= 4 ? 0xff : 0;
		} else {
			a = pixels.get(offset + 3);
		}

		return (r << 24) | (g << 16) | (b << 8) | a;
	}

	public void drawPixel(int x, int y, int color) {
		if (pixelStride != 4) {
			throw new GdxRuntimeException("Unsupported format");
		}
		int offset = (this.y + y) * lineStride + (this.x + x) * pixelStride;
		pixels.putInt(offset, color);
	}

	public void setRegion(PixmapRegion region) {
		changePixmap(region.pixmap);
		this.x = region.x;
		this.y = region.y;
		this.width = region.width;
		this.height = region.height;
	}

	public void setRegion(Pixmap pixmap, int x, int y, int width, int height) {
		changePixmap(pixmap);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public int getRegionX() {
		return x;
	}

	public int getRegionY() {
		return y;
	}

	public int getRegionWidth() {
		return width;
	}

	public int getRegionHeight() {
		return height;
	}

	public TextureRegion getRegion(TextureRegion region) {
		region.setRegion(x, y, width, height);
		return region;
	}

	public Pixmap.Format getFormat() {
		return pixmap.getFormat();
	}

	@Override
	public boolean equals(Object other) {

		if (other instanceof PixmapRegion) {

			PixmapRegion region = (PixmapRegion) other;

			return (this.pixmap == region.pixmap)
					&& (this.x == region.x) && (this.y == region.y)
					&& (this.width == region.width) && (this.height == region.height);
		}

		return false;
	}

	@Override
	public int hashCode() {
		assert false : "not implemented";
		return -1;
	}

}
