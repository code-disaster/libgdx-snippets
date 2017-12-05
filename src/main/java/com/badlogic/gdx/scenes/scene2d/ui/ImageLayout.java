package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.json.annotations.JsonSerialize;

@SuppressWarnings("unused")
public class ImageLayout extends ActorLayout<Image> {

	@JsonSerialize
	public String drawable;

	public ImageLayout() {
		super(Image.class);
	}

	@Override
	protected Image createActor(Skin skin, StageLayoutListener listener) {
		Image image = new Image(skin, drawable);

		if (layout.width > 0) {
			image.setWidth(layout.width);
		}

		if (layout.height > 0) {
			image.setHeight(layout.height);
		}

		return image;
	}

}
