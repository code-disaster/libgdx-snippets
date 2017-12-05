package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

@SuppressWarnings("unused")
public class ButtonLayout extends ActorLayout<Button> {

	public ButtonLayout() {
		super(Button.class);
	}

	@Override
	protected Button createActor(Skin skin, StageLayoutListener listener) {

		Button button = new Button(skin, style);
		button.addListener(new StageLayoutClickListener(listener, nameId));

		return button;
	}

	public static class StageLayoutClickListener extends ClickListener {

		private StageLayoutListener listener;
		private int nameId;

		public StageLayoutClickListener(StageLayoutListener listener, int nameId) {
			this.listener = listener;
			this.nameId = nameId;
		}

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
	}

}
