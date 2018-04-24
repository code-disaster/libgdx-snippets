package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;

import java.nio.ByteBuffer;
import java.util.function.IntPredicate;

import static com.badlogic.gdx.graphics.Pixmap.*;

public class PixmapUtils {

	/**
	 * Create {@link Pixmap} with no blending and nearest filter.
	 */
	public static Pixmap create(int width, int height, Format format) {
		return create(width, height, format, Blending.None, Filter.NearestNeighbour);
	}

	/**
	 * Create {@link Pixmap} with given blending and filter.
	 */
	public static Pixmap create(int width, int height, Format format,
								Blending blending, Filter filter) {

		Pixmap pixmap = new Pixmap(width, height, format);

		pixmap.setBlending(blending);
		pixmap.setFilter(filter);

		return pixmap;
	}

	/**
	 * Sets alpha for all pixels matching one of the RGB values in {@code colors[]}.
	 */
	public static void mask(Pixmap pixmap, float alpha, Color... colors) {

		ByteBuffer pixels = pixmap.getPixels();

		int[] masks = new int[colors.length];
		for (int i = 0; i < colors.length; i++) {
			masks[i] = colors[i].toIntBits();
			masks[i] = ((masks[i] & 0xff0000) >> 16) | (masks[i] & 0xff00) | ((masks[i] & 0xff) << 16);
		}

		int a = MathUtils.floor(alpha * 255.0f) & 0xff;

		while (pixels.remaining() > 0) {

			int rgba = pixels.getInt();
			int rgb = rgba >>> 8;

			for (int mask : masks) {
				if (rgb == mask) {
					pixels.position(pixels.position() - 4);
					pixels.putInt((rgba & 0xffffff00) | a);
					break;
				}
			}
		}

		pixels.flip();
	}

	/**
	 * Sets alpha for all pixels passing the RGB predicate function.
	 */
	public static void mask(Pixmap pixmap, float alpha, IntPredicate rgb) {

		ByteBuffer pixels = pixmap.getPixels();

		int a = MathUtils.floor(alpha * 255.0f) & 0xff;

		while (pixels.remaining() > 0) {

			int rgba = pixels.getInt();

			if (rgb.test(rgba >>> 8)) {
				pixels.position(pixels.position() - 4);
				pixels.putInt((rgba & 0xffffff00) | a);
			}
		}

		pixels.flip();
	}

	public interface CropResult {

		void accept(int left, int bottom, int width, int height);
	}

	/**
	 * Calculates crop regions of the pixmap in up to four directions. Pixel rows/columns are subject
	 * to removal if all their pixels have an alpha channel value of exact the same value as given in the parameter.
	 */
	public static void crop(Pixmap pixmap,
							boolean left,
							boolean bottom,
							boolean right,
							boolean top,
							float alpha,
							CropResult consumer) {

		int width = pixmap.getWidth();
		int height = pixmap.getHeight();

		int a = MathUtils.floor(alpha * 255.0f) & 0xff;

		int minX = left ? width - 1 : 0;
		int maxX = right ? 0 : width - 1;

		int minY = bottom ? height - 1 : 0;
		int maxY = top ? 0 : height - 1;

		ByteBuffer pixels = pixmap.getPixels();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				int rgba = pixels.getInt();

				if ((rgba & 0xff) != a) {

					minX = Math.min(x, minX);
					maxX = Math.max(x, maxX);

					minY = Math.min(y, minY);
					maxY = Math.max(y, maxY);
				}
			}
		}

		pixels.flip();

		consumer.accept(minX, minY, maxX, maxY);
	}

	/**
	 * Returns a copy of the given pixmap, cropped about the given dimensions.
	 */
	public static Pixmap crop(Pixmap pixmap, int left, int bottom, int width, int height) {

		Pixmap result = new Pixmap(width, height, pixmap.getFormat());
		result.drawPixmap(pixmap, 0, 0, left, bottom, width, height);

		return result;
	}

	/**
	 * Vertically mirrors the {@link Pixmap} content, in place, line by line.
	 */
	public static void flipY(Pixmap pixmap) {

		int width = pixmap.getWidth();
		int height = pixmap.getHeight();

		int pitch = width * getPixelStride(pixmap.getFormat());

		ByteBuffer pixels = pixmap.getPixels();

		byte[][] buffer = new byte[2][pitch];

		for (int y = 0; y < height / 2; y++) {

			pixels.position(y * pitch);
			pixels.get(buffer[0], 0, pitch);

			pixels.position((height - y - 1) * pitch);
			pixels.get(buffer[1], 0, pitch);

			pixels.position(y * pitch);
			pixels.put(buffer[1], 0, pitch);

			pixels.position((height - y - 1) * pitch);
			pixels.put(buffer[0], 0, pitch);
		}

		pixels.position(0);
	}

	public static int getPixelStride(Format format) {
		switch (format) {
			case Alpha:
			case Intensity:
				return 1;
			case LuminanceAlpha:
			case RGB565:
			case RGBA4444:
				return 2;
			case RGB888:
				return 3;
			case RGBA8888:
				return 4;
		}
		throw new IllegalArgumentException("Not implemented");
	}

}
