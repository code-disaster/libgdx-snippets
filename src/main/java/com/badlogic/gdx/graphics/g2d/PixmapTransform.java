package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Pixmap;

public class PixmapTransform {

	public enum Mirror {
		None,
		X,
		Y,
		XY
	}

	public enum Rotate {
		None,
		_90,
		_180,
		_270
	}

	/**
	 * Read pixel from {@link Pixmap}, using back-transformed flipped and rotated pixel coordinates.
	 */
	public static int getPixel(Pixmap pixmap, int x, int y, Mirror mirror, Rotate rotate) {

		int widthMinusOne = pixmap.getWidth() - 1;
		int heightMinusOne = pixmap.getHeight() - 1;

		int px = x, py = y;
		int outX, outY;

		// mirror

		if (mirror == Mirror.X || mirror == Mirror.XY) {
			px = widthMinusOne - x;
		}

		if (mirror == Mirror.Y || mirror == Mirror.XY) {
			py = heightMinusOne - y;
		}

		// rotate

		switch (rotate) {
			case _90:
				outX = py;
				outY = heightMinusOne - px;
				break;
			case _180:
				outX = widthMinusOne - px;
				outY = heightMinusOne - py;
				break;
			case _270:
				outX = widthMinusOne - py;
				outY = px;
				break;
			default:
			case None:
				outX = px;
				outY = py;
				break;
		}

		return pixmap.getPixel(outX, outY);
	}

	public static int getPixelRotated(Pixmap pixmap, int x, int y, Rotate rotate) {
		int widthMinusOne = pixmap.getWidth() - 1;
		int heightMinusOne = pixmap.getHeight() - 1;

		int px = x, py = y;
		int outX, outY;



		// rotate

		switch (rotate) {
			case _90:
				outX = py;
				outY = heightMinusOne - px;
				break;
			case _180:
				outX = widthMinusOne - px;
				outY = heightMinusOne - py;
				break;
			case _270:
				outX = widthMinusOne - py;
				outY = px;
				break;
			default:
			case None:
				outX = px;
				outY = py;
				break;
		}

		return pixmap.getPixel(outX, outY);
	}

	public static int getPixelMirrored(Pixmap pixmap, int x, int y, Mirror mirror) {
		int widthMinusOne = pixmap.getWidth() - 1;
		int heightMinusOne = pixmap.getHeight() - 1;

		int px = x, py = y;

		if (mirror == Mirror.X || mirror == Mirror.XY) {
			px = widthMinusOne - x;
		}

		if (mirror == Mirror.Y || mirror == Mirror.XY) {
			py = heightMinusOne - y;
		}

		return pixmap.getPixel(px, py);
	}

	public static Pixmap getRotatedMirroredCopy(Pixmap pixmap, Mirror mirror, Rotate rotate){

		Pixmap mirrored = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());
		copyToMirrored(pixmap, mirrored, mirror);

		int newWidth = PixmapTransform.getWidth(pixmap, rotate);
		int newHeight = PixmapTransform.getHeight(pixmap, rotate);

		Pixmap copy = new Pixmap(newWidth, newHeight, pixmap.getFormat());
		copyToRotated(mirrored, copy, rotate);

		/** Only temp for copying around */
		mirrored.dispose();

		return copy;
	}

	public static void copyToRotated(Pixmap source, Pixmap target, Rotate rotate){
		for(int y=0; y<target.getHeight(); y++){
			for(int x=0; x<target.getWidth(); x++){
				int rgba = PixmapTransform.getPixelRotated(source, x, y, rotate);
				target.drawPixel(x, y, rgba);
			}
		}
	}

	public static void copyToMirrored(Pixmap source, Pixmap target, Mirror mirror){
		for(int y=0; y<target.getHeight(); y++){
			for(int x=0; x<target.getWidth(); x++){
				int rgba = PixmapTransform.getPixelMirrored(source, x, y, mirror);
				target.drawPixel(x, y, rgba);
			}
		}
	}

	/**
	 * mirror pixmap in-place, no-op for {@link Mirror#None}
	 */
	public static void mirror(Pixmap pixmap, Mirror mirror) {

		if (mirror == Mirror.X || mirror == Mirror.XY) {
			PixmapUtils.flipX(pixmap);
		}

		if (mirror == Mirror.Y || mirror == Mirror.XY) {
			PixmapUtils.flipY(pixmap);
		}
	}

	public static int getWidth(int width, int height, Rotate rotate) {
		return (rotate.ordinal() & 0x1) == 1 ? height : width;
	}

	public static int getHeight(int width, int height, Rotate rotate) {
		return (rotate.ordinal() & 0x1) == 1 ? width : height;
	}

	public static int getWidth(Pixmap pixmap, Rotate rotate) {
		return getWidth(pixmap.getWidth(), pixmap.getHeight(), rotate);
	}

	public static int getHeight(Pixmap pixmap, Rotate rotate) {
		return getHeight(pixmap.getWidth(), pixmap.getHeight(), rotate);
	}

}
