package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.*;

/**
 * Loads images from texture atlas, just like {@link TextureAtlas}, but stores them in pixmaps instead of textures
 * to avoid creation of texture resources.
 * <p>
 * Internally uses {@link com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData} to avoid code duplication.
 */
public class PixmapAtlas implements Disposable {

	private final ObjectSet<Pixmap> pixmaps = new ObjectSet<>(4);
	private final Array<AtlasPage> pages = new Array<>();
	private final Array<AtlasRegion> regions = new Array<>();

	public PixmapAtlas(FileHandle packFile) {
		this(packFile, packFile.parent(), false);
	}

	public PixmapAtlas(FileHandle packFile, PixmapReader reader) {
		load(new TextureAtlas.TextureAtlasData(packFile, packFile.parent(), false), reader);
	}

	public PixmapAtlas(FileHandle packFile, FileHandle imagesDir, boolean flip) {
		load(new TextureAtlas.TextureAtlasData(packFile, imagesDir, flip), DEFAULT_READER);
	}

	public PixmapAtlas(TextureAtlas.TextureAtlasData data) {
		load(data, DEFAULT_READER);
	}

	private PixmapAtlas() {

	}

	private void load(TextureAtlas.TextureAtlasData data, PixmapReader reader) {

		ObjectMap<TextureAtlas.TextureAtlasData.Page, AtlasPage> pageToPixmap = new ObjectMap<>();

		for (int pageIndex = 0; pageIndex < data.pages.size; pageIndex++) {

			TextureAtlas.TextureAtlasData.Page page = data.pages.get(pageIndex);

			Pixmap pixmap = reader.read(page.textureFile, (int) page.width, (int) page.height);
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

	public FileHandle getFolder() {
		return pages.get(0).fileHandle.parent();
	}

	public static PixmapAtlas createFromPixmap(FileHandle pixmapFile, String regionName) {

		Pixmap pixmap = new Pixmap(pixmapFile);

		PixmapAtlas atlas = new PixmapAtlas();

		// add single pixmap
		atlas.pixmaps.add(pixmap);

		// add one page
		AtlasPage atlasPage = new AtlasPage(0, pixmapFile, pixmap);
		atlas.pages.add(atlasPage);

		// add one region
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();

		AtlasRegion atlasRegion = new AtlasRegion(pixmap, 0, 0, width, height);

		atlasRegion.page = atlasPage;
		atlasRegion.index = 0;
		atlasRegion.name = regionName;
		atlasRegion.rotate = false;

		atlas.regions.add(atlasRegion);

		return atlas;
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

	@FunctionalInterface
	public interface PixmapReader {

		Pixmap read(FileHandle file, int width, int height);
	}

	private static PixmapReader DEFAULT_READER = (file, width, height) -> new Pixmap(file);

}
