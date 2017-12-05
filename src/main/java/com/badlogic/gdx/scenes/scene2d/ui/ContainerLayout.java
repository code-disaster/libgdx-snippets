package com.badlogic.gdx.scenes.scene2d.ui;

public class ContainerLayout extends ActorLayout<Container> {

	public ContainerLayout(){
		super(Container.class);
	}

	@Override
	protected Container createActor(Skin skin, StageLayoutListener listener) {
		Container container;

		container = new Container();
		container.setSize(layout.width, layout.height);

		return container;
	}
}
