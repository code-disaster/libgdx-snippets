package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.json.annotations.JsonSerialize;

import static com.badlogic.gdx.utils.Align.*;

@SuppressWarnings("unused")
public class LabelLayout extends ActorLayout<Label> {

	@JsonSerialize
	public String label;

	@JsonSerialize
	public Align align = Align.Center;

	@JsonSerialize
	public boolean wrap = false;

	public enum Align {
		Center,
		Left,
		Right
	}

	public LabelLayout() {
		super(Label.class);
	}

	@Override
	protected Label createActor(Skin skin,
								StageLayoutListener listener) {

		String text = listener.getTranslation(label);
		Label actor = new Label(text, skin, style);

		switch (align) {
			case Center:
				actor.setAlignment(center);
				break;
			case Left:
				actor.setAlignment(left);
				break;
			case Right:
				actor.setAlignment(right);
				break;
		}

		actor.setWrap(wrap);

		if (layout.width > 0)
			actor.setWidth(layout.width);
		if (layout.height > 0)
			actor.setHeight(layout.height);

		return actor;
	}

}
