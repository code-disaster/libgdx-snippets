package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

class StageLayoutContainer<T extends Actor> extends Container<T> {

	private Container<?> envelope;

	StageLayoutContainer(T actor, Container<?> envelope) {
		super(actor);
		this.envelope = envelope;
	}

	static Container<?> getEnvelope(Container<?> container) {

		if (container instanceof StageLayoutContainer<?>) {
			return ((StageLayoutContainer<?>) container).envelope;
		}

		return null;
	}

}
