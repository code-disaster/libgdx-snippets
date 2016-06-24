package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Pixmap;

/**
 * Defines a rectangular area of a {@link Pixmap}.
 */
public class PixmapRegion {

	private Pixmap pixmap;
	private int x, y;
	private int width, height;

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

	public Pixmap getPixmap() {
		return pixmap;
	}

	public int getPixel(int x, int y) {
		return pixmap.getPixel(this.x + x, this.y + y);
	}

	public void drawPixel(int x, int y, int color) {
		pixmap.drawPixel(this.x + x, this.y + y, color);
	}

	public void setRegion(PixmapRegion region) {
		this.pixmap = region.pixmap;
		this.x = region.x;
		this.y = region.y;
		this.width = region.width;
		this.height = region.height;
	}

	public void setRegion(Pixmap pixmap, int x, int y, int width, int height) {
		this.pixmap = pixmap;
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
