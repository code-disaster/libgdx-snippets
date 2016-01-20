package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.*;

/**
 * Loads images from texture atlas, just like {@link TextureAtlas}, but stores them in pixmaps instead of textures
 * to avoid creation of texture resources.
 *
 * Internally uses {@link com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData} to avoid code duplication.
 */
public class PixmapAtlas implements Disposable {

	private final ObjectSet<Pixmap> pixmaps = new ObjectSet<>(4);
	private final Array<AtlasPage> pages = new Array<>();
	private final Array<AtlasRegion> regions = new Array<>();

	public PixmapAtlas(FileHandle packFile) {
		this(packFile, packFile.parent(), false);
	}

	public PixmapAtlas(FileHandle packFile, FileHandle imagesDir, boolean flip) {
		this(new TextureAtlas.TextureAtlasData(packFile, imagesDir, flip));
	}

	public PixmapAtlas(TextureAtlas.TextureAtlasData data) {
		load(data);
	}

	private void load(TextureAtlas.TextureAtlasData data) {

		ObjectMap<TextureAtlas.TextureAtlasData.Page, AtlasPage> pageToPixmap = new ObjectMap<>();

		for (int pageIndex = 0; pageIndex < data.pages.size; pageIndex++) {

			TextureAtlas.TextureAtlasData.Page page = data.pages.get(pageIndex);

			Pixmap pixmap = new Pixmap(page.textureFile);
			pixmaps.add(pixmap);

			AtlasPage atlasPage = new AtlasPage(pageIndex, page.textureFile, pixmap);
			pages.add(atlasPage);

			pageToPixmap.put(page, atlasPage);
		}

		for (TextureAtlas.TextureAtlasData.Region region : data.regions) {

			int width = region.width;
			int height = region.height;

			AtlasPage atlasPage = pageToPixmap.get(region.page);

			AtlasRegion atlasRegion = new AtlasRegion(
					atlasPage.pixmap,
					region.left,
					region.top,
					region.rotate ? height : width,
					region.rotate ? width : height);

			atlasRegion.page = atlasPage;
			atlasRegion.index = region.index;
			atlasRegion.name = region.name;
			atlasRegion.rotate = region.rotate;

			regions.add(atlasRegion);
		}
	}

	@Override
	public void dispose() {
		pixmaps.forEach(Pixmap::dispose);
		pixmaps.clear();
		pages.clear();
		regions.clear();
	}

	public ObjectSet<Pixmap> getPixmaps() {
		return pixmaps;
	}

	public Iterable<AtlasPage> getPages() {
		return pages;
	}

	public AtlasRegion findRegion(String name) {
		for (AtlasRegion region : regions) {
			if (region.name.equals(name)) {
				return region;
			}
		}
		return null;
	}

	public static class AtlasRegion extends PixmapRegion {

		public AtlasPage page;

		// todo: "copy" required fields from TextureAtlas$AtlasRegion
		int index;
		String name;
		boolean rotate;

		AtlasRegion(Pixmap pixmap, int x, int y, int width, int height) {
			super(pixmap, x, y, width, height);
		}
	}

	public static class AtlasPage {

		public final int pageIndex;
		public final FileHandle fileHandle;
		public final Pixmap pixmap;

		AtlasPage(int pageIndex, FileHandle fileHandle, Pixmap pixmap) {
			this.pageIndex = pageIndex;
			this.fileHandle = fileHandle;
			this.pixmap = pixmap;
		}
	}
}
