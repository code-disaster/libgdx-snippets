package com.badlogic.gdx.scenes.scene2d.ui;

public class StageLayoutAdapter implements StageLayoutListener {

	private StageLayoutListener listener;

	public StageLayoutAdapter(StageLayoutListener listener) {
		this.listener = listener;
	}

	@Override
	public void onButtonDown(int layoutId) {
		listener.onButtonDown(layoutId);
	}

	@Override
	public void onButtonUp(int layoutId) {
		listener.onButtonUp(layoutId);
	}

	@Override
	public void onMouseOver(int layoutId) {
		listener.onMouseOver(layoutId);
	}
	
}
