package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ButtonLayout extends ActorLayout<Button> {

	@Override
	protected Button createActor(Skin skin,
								 String styleName,
								 Function<String, ActorLayout<?>> layouts,
								 Consumer<Actor> registry,
								 StageLayoutListener listener) {

		Button button = new Button(skin, styleName);

		button.addListener(new ClickListener() {

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (super.touchDown(event, x, y, pointer, button)) {
					listener.onButtonDown(nameId);
					return true;
				}
				return false;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				listener.onButtonUp(nameId);
			}

			@Override
			public boolean mouseMoved(InputEvent event, float x, float y) {
				listener.onMouseOver(nameId);
				return super.mouseMoved(event, x, y);
			}
		});

		return button;
	}

}
