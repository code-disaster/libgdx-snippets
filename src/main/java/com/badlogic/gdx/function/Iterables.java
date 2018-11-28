package com.badlogic.gdx.function;

public final class Iterables {

	/**
	 * Substitute for {@link Iterable#forEach(java.util.function.Consumer)}.
	 */
	public static <T> void forEach(Iterable<T> iterable, Consumer<? super T> action) {
		for (T t : iterable) {
			action.accept(t);
		}
	}

}
