package com.badlogic.gdx.concurrent;

import com.badlogic.gdx.function.Consumer;
import com.badlogic.gdx.function.Supplier;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * A functional style wrapper to {@link java.util.concurrent.locks.ReentrantLock}.
 */
public class ReentrantLock {

	private final Lock lock = new java.util.concurrent.locks.ReentrantLock();

	public void lock(Runnable runnable) {
		try {
			lock.lock();
			runnable.run();
		} finally {
			lock.unlock();
		}
	}

	public <C> void lock(C context, Consumer<C> consumer) {
		try {
			lock.lock();
			consumer.accept(context);
		} finally {
			lock.unlock();
		}
	}

	public <T> T lock(Supplier<T> supplier) {
		try {
			lock.lock();
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}

	public <T> T tryLock(Supplier<T> supplier, T defaultValue) {
		if (!lock.tryLock()) {
			return defaultValue;
		}
		try {
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}

	public <T> T tryLock(Supplier<T> supplier, T defaultValue, long time, TimeUnit unit) throws InterruptedException {
		if (!lock.tryLock(time, unit)) {
			return defaultValue;
		}
		try {
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}

}
