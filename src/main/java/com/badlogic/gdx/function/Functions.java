package com.badlogic.gdx.function;

import java.util.Comparator;

public final class Functions {

	public static final Runnable NOOP_RUNNABLE = () -> {
	};

	@SuppressWarnings("ComparatorCombinators")
	public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
			Function<? super T, ? extends U> fn) {
		return (o1, o2) -> fn.apply(o1).compareTo(fn.apply(o2));
	}

	@SuppressWarnings("ComparatorCombinators")
	public static <T> Comparator<T> comparingInt(ToIntFunction<T> fn) {
		return (o1, o2) -> Integer.compare(fn.applyAsInt(o1), fn.applyAsInt(o2));
	}

	public static <T> Comparator<T> comparingIntDescending(ToIntFunction<T> fn) {
		return (o1, o2) -> Integer.compare(fn.applyAsInt(o2), fn.applyAsInt(o1));
	}

}
