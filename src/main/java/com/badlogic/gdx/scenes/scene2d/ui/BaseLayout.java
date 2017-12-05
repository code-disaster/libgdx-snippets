package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.json.annotations.JsonSerializable;
import com.badlogic.gdx.json.annotations.JsonSerialize;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.viewport.Viewport;

@JsonSerializable
public class BaseLayout {

	@JsonSerialize
	public int x = 0;

	@JsonSerialize
	public int y = 0;

	@JsonSerialize
	public int width = 0;

	@JsonSerialize
	public int height = 0;

	@JsonSerialize
	public Align anchor = Align.Center;

	@JsonSerialize
	public boolean touchable = true;

	public enum Align {
		Center,
		Left,
		Right,
		Bottom,
		Top,
		BottomLeft,
		BottomRight,
		TopLeft,
		TopRight
	}

	<T extends Actor> void resize(Stage stage, Actor actor, boolean root) {

		int parentWidth, parentHeight;

		if (root) {
			Viewport viewport = stage.getViewport();

			parentWidth = viewport.getScreenWidth();
			parentHeight = viewport.getScreenHeight();

		} else {
			Actor parent = actor.getParent();

			parentWidth = MathUtils.floor(parent.getWidth());
			parentHeight = MathUtils.floor(parent.getHeight());
		}

		int actorWidth = MathUtils.floor(actor.getWidth());
		int actorHeight = MathUtils.floor(actor.getHeight());

		/** Dynamic sizing of actor if we want percentage of parent (indicated by negative width) */
		if(width < 0){
			actorWidth = MathUtils.clamp(-width, 1, 100) * parentWidth / 100;

			actor.setWidth(actorWidth);
		}
		if(height < 0){
			actorHeight = MathUtils.clamp(-height, 1, 100) * parentHeight / 100;

			actor.setHeight(actorHeight);
		}

		int px = 0, py = 0;

		switch (anchor) {

			case Center:
				px = parentWidth / 2 - actorWidth / 2;
				py = parentHeight / 2 - actorHeight / 2;
				break;

			case Left:
				px = 0;
				py = parentHeight / 2 - actorHeight / 2;
				break;

			case Right:
				px = parentWidth - actorWidth;
				py = parentHeight / 2 - actorHeight / 2;
				break;

			case Bottom:
				px = parentWidth / 2 - actorWidth / 2;
				py = 0;
				break;

			case Top:
				px = parentWidth / 2 - actorWidth / 2;
				py = parentHeight - actorHeight;
				break;

			case BottomLeft:
				px = 0;
				py = 0;
				break;

			case BottomRight:
				px = parentWidth - actorWidth;
				py = 0;
				break;

			case TopLeft:
				px = 0;
				py = parentHeight - actorHeight;
				break;

			case TopRight:
				px = parentWidth - actorWidth;
				py = parentHeight - actorHeight;
				break;
		}

		px += x;
		py += y;

		actor.setPosition(px, py);
	}

}
