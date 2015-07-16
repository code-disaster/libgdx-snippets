package com.badlogic.gdx.concurrent;

import com.badlogic.gdx.utils.Disposable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncTaskExecutor implements Disposable {

	private ExecutorService service;

	public AsyncTaskExecutor(int threadCount) {

		if (threadCount <= 1) {
			service = Executors.newSingleThreadExecutor();
		} else {
			service = Executors.newFixedThreadPool(threadCount);
		}
	}

	public <R extends Callable<R>>
	void execute(AsyncTask<R> task) {
		task.execute(service);
	}

	@Override
	public void dispose() {

		service.shutdown();

		try {
			service.awaitTermination(2500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
