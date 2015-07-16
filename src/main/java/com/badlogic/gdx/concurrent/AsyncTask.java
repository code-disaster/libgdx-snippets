package com.badlogic.gdx.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A wrapper for {@link FutureTask} with additional properties.
 */
public class AsyncTask<R extends Callable<R>> {

	public interface CompletionHandler<R extends Callable<R>> {
		void run(R callable);
	}

	private enum State {
		READY,
		PENDING,
		DONE
	}

	protected final R callable;

	private final AtomicReference<R> result = new AtomicReference<>();
	private final AtomicReference<State> state = new AtomicReference<>(State.READY);
	private final AtomicBoolean resultAvailable = new AtomicBoolean(false);

	public AsyncTask(R callable) {
		this.callable = callable;
		result.set(callable);
	}

	public boolean isPending() {
		return state.get() == State.PENDING;
	}

	public boolean isDone() {
		return state.get() == State.DONE;
	}

	public void await() {
		while (isPending()) {
			Thread.yield();
		}
	}

	public void poll(CompletionHandler<R> completionHandler) {
		if (state.get() == State.DONE) {
			if (resultAvailable.compareAndSet(true, false)) {
				R r = get();
				if (r != null) {
					completionHandler.run(get());
				}
			}
		}
	}

	public R get() {
		if (state.get() != State.DONE) {
			throw new IllegalStateException("Illegal state!");
		}
		state.compareAndSet(State.DONE, State.READY);
		return result.get();
	}

	void execute(ExecutorService service) {

		if (state.get() == State.PENDING) {
			throw new IllegalStateException("Task still pending!");
		}

		if (state.get() == State.DONE) {
			throw new IllegalStateException("Task result not requested!");
		}

		// reset state
		result.set(null);
		state.compareAndSet(State.READY, State.PENDING);
		resultAvailable.set(false);

		// pass to executor service
		service.execute(new Task(callable));
	}

	private class Task extends FutureTask<R> {

		Task(R callable) {
			super(callable);
		}

		@Override
		protected void done() {

			try {

				if (!state.compareAndSet(State.PENDING, State.DONE)
						|| !resultAvailable.compareAndSet(false, true)) {
					throw new IllegalStateException("Invalid completion state!");
				}

				if (!result.compareAndSet(null, get())) {
					throw new RuntimeException("Invalid result state!");
				}


			} catch (InterruptedException | ExecutionException e) {
				throw new IllegalStateException(e);
			}

		}
	}
}
