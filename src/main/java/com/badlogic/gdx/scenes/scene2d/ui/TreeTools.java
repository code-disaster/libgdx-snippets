package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public final class TreeTools {

	public static ClickListener createNodeClickListener(Tree tree, Runnable clicked) {

		return new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				Tree.Node node = tree.getNodeAt(y);

				if (node != null && node.getChildren().size > 0) {
					node.setExpanded(!node.isExpanded());
					tree.getClickListener().clicked(event, x, y);
				}

				clicked.run();
			}
		};
	}

}
