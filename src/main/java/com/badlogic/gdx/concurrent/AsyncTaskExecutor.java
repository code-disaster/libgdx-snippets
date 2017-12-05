package com.badlogic.gdx.concurrent;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxSnippets;

import javax.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A wrapper to {@link ExecutorService} which executes {@link AsyncTask} tasks.
 */
public class AsyncTaskExecutor implements Disposable {

	private final ExecutorService service;

	public AsyncTaskExecutor(int threadCount, String threadNamePrefix) {

		threadCount = Math.max(threadCount, 1);
		GdxSnippets.log.info("Starting {} with {} threads.", threadNamePrefix, threadCount);

		service = new FixedThreadPoolExecutor(threadCount, new Factory(threadNamePrefix));
	}

	public <V extends AsyncTaskJob<V>>
	void execute(AsyncTask<V> task) {
		task.execute(service);
	}

	@Override
	public void dispose() {

		service.shutdown();
		GdxSnippets.log.info("Shutting down AsyncTaskExecutor");

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
			Thread thread = new Thread(group, runnable, namePrefix + "-" + threadNumber.getAndIncrement());
			thread.setDaemon(true);
			return thread;
		}
	}

	private static class FixedThreadPoolExecutor extends ThreadPoolExecutor {

		FixedThreadPoolExecutor(int nThreads, ThreadFactory threadFactory) {
			super(nThreads, nThreads,
					0L, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<>(),
					threadFactory);
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			if (t == null && r instanceof Future<?>) {
				try {
					Object result = ((Future<?>) r).get();
				} catch (CancellationException ce) {
					t = ce;
				} catch (ExecutionException ee) {
					t = ee.getCause();
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt(); // ignore/reset
				}
			}
			if (t != null) {
				GdxSnippets.log.error("thread pool execution error", t);
			}
		}
	}

}
