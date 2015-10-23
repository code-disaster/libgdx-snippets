package com.badlogic.gdx.concurrent;

import com.badlogic.gdx.utils.Disposable;

import javax.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A wrapper to {@link ExecutorService} which executes {@link AsyncTask} tasks.
 */
public class AsyncTaskExecutor implements Disposable {

	private final ExecutorService service;

	public AsyncTaskExecutor(int threadCount) {

		threadCount = Math.max(threadCount, 1);
		System.out.println("Starting AsyncTaskExecutor with " + threadCount + " threads.");

		if (threadCount <= 1) {
			service = Executors.newSingleThreadExecutor(new Factory("AsyncTask-Single-"));
		} else {
			service = Executors.newFixedThreadPool(threadCount, new Factory("AsyncTask-Pool-"));
		}
	}

	public <R extends Callable<R>>
	void execute(AsyncTask<R> task) {
		task.execute(service);
	}

	@Override
	public void dispose() {

		service.shutdown();
		System.out.println("Shutting down AsyncTaskExecutor");

		try {
			service.awaitTermination(2500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static class Factory implements ThreadFactory {

		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		Factory(String prefix) {
			group = Thread.currentThread().getThreadGroup();
			namePrefix = prefix;
		}

		@Override
		public Thread newThread(@Nonnull Runnable runnable) {
			Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement());
			thread.setDaemon(true);
			return thread;
		}
	}

}
