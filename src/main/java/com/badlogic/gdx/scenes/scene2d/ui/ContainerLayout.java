package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.json.annotations.JsonSerializable;
import com.badlogic.gdx.json.annotations.JsonSerialize;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

@JsonSerializable
public class ContainerLayout {

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

	private static Vector2 tmpVec2 = new Vector2();

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

	<T extends Actor> void resize(Stage stage, Container<T> container) {

		int parentX, parentY;
		int parentWidth, parentHeight;

		Container<?> envelope = StageLayoutContainer.getEnvelope(container);
		boolean root = envelope == null;

		if (root) {

			Viewport viewport = stage.getViewport();

			parentX = 0;
			parentY = 0;

			parentWidth = viewport.getScreenWidth();
			parentHeight = viewport.getScreenHeight();

		} else {

			Actor parent = envelope.getActor();

			tmpVec2.set(0.0f, 0.0f);
			parent.localToStageCoordinates(tmpVec2);

			parentX = MathUtils.floor(tmpVec2.x);
			parentY = MathUtils.floor(tmpVec2.y);

			parentWidth = MathUtils.floor(parent.getWidth());
			parentHeight = MathUtils.floor(parent.getHeight());
		}

		T actor = container.getActor();

		int actorWidth = MathUtils.floor(actor.getWidth());
		int actorHeight = MathUtils.floor(actor.getHeight());

		int px = 0, py = 0;

		if (root) {

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

		} else {

			switch (anchor) {

				case Center:
					px = parentX + parentWidth / 2 - actorWidth / 2;
					py = parentY + parentHeight / 2 - actorHeight / 2;
					break;

				case Left:
					px = parentX;
					py = parentY + parentHeight / 2 - actorHeight / 2;
					break;

				case Right:
					px = parentX + parentWidth - actorWidth;
					py = parentY + parentHeight / 2 - actorHeight / 2;
					break;

				case Bottom:
					px = parentX + parentWidth / 2 - actorWidth / 2;
					py = parentY;
					break;

				case Top:
					px = parentX + parentWidth / 2 - actorWidth / 2;
					py = parentY + parentHeight - actorHeight;
					break;

				case BottomLeft:
					px = parentX;
					py = parentY;
					break;

				case BottomRight:
					px = parentX + parentWidth - actorWidth;
					py = parentY;
					break;

				case TopLeft:
					px = parentX;
					py = parentY + parentHeight - actorHeight;
					break;

				case TopRight:
					px = parentX + parentWidth - actorWidth;
					py = parentY + parentHeight - actorHeight;
					break;
			}
		}

		px += x;
		py += y;

		container.setPosition(px, py);
	}

}
