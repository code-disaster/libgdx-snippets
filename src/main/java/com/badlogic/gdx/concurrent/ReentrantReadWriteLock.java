package com.badlogic.gdx.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A functional style wrapper to {@link java.util.concurrent.locks.ReentrantReadWriteLock}.
 */
public class ReentrantReadWriteLock {

	private final ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

	public void read(Runnable runnable) {
		try {
			lock.readLock().lock();
			runnable.run();
		} finally {
			lock.readLock().unlock();
		}
	}

	public <C> void read(C context, Consumer<C> consumer) {
		try {
			lock.readLock().lock();
			consumer.accept(context);
		} finally {
			lock.readLock().unlock();
		}
	}

	public <T> T read(Supplier<T> supplier) {
		try {
			lock.readLock().lock();
			return supplier.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	public <T> T tryRead(Supplier<T> supplier, T defaultValue) {
		if (!lock.readLock().tryLock()) {
			return defaultValue;
		}
		try {
			return supplier.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	public <T> T tryRead(Supplier<T> supplier, T defaultValue, long time, TimeUnit unit) throws InterruptedException {
		if (!lock.readLock().tryLock(time, unit)) {
			return defaultValue;
		}
		try {
			return supplier.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	public void write(Runnable runnable) {
		try {
			lock.writeLock().lock();
			runnable.run();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public <T> T write(Supplier<T> supplier) {
		try {
			lock.writeLock().lock();
			return supplier.get();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public <T> T tryWrite(Supplier<T> supplier, T defaultValue) {
		if (!lock.writeLock().tryLock()) {
			return defaultValue;
		}
		try {
			return supplier.get();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public <T> T tryWrite(Supplier<T> supplier, T defaultValue, long time, TimeUnit unit) throws InterruptedException {
		if (!lock.writeLock().tryLock(time, unit)) {
			return defaultValue;
		}
		try {
			return supplier.get();
		} finally {
			lock.writeLock().unlock();
		}
	}

}
