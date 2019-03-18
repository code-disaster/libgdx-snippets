package com.badlogic.gdx.concurrent;

import com.badlogic.gdx.function.Supplier;

/**
 * Substitute for {@link ThreadLocal#withInitial(Supplier)}
 */
public class ThreadLocalInstance<T> extends ThreadLocal<T> {

	private final Supplier<? extends T> supplier;

	public ThreadLocalInstance(Supplier<? extends T> supplier) {
		this.supplier = supplier;
	}

	@Override
	protected T initialValue() {
		return supplier.get();
	}
}
