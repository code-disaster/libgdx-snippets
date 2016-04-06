package com.badlogic.gdx.concurrent;

import java.util.concurrent.Callable;

public interface AsyncTaskJob<V> extends Callable<V> {

	void completed();

}
