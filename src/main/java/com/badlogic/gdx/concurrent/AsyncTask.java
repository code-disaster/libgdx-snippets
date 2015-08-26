package com.badlogic.gdx.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * A reentrant wrapper to {@link FutureTask}, with some additional properties to synchronize its result with the
 * calling thread.
 * <p>
 * The task is (re-)scheduled for asynchronous execution with {@link AsyncTask#execute(ExecutorService)}.
 * <p>
 * Upon completion, result of the asynchronous computation is cached. It can then be read or polled by the calling
 * thread via {@link AsyncTask#get()} or {@link AsyncTask#poll(Consumer)}.
 */
public abstract class AsyncTask<R extends Callable<R>> {

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

	/**
	 * This function blocks until the asynchronous task has been completed, if one is pending.
	 */
	public void await() {
		while (isPending()) {
			Thread.yield();
		}
	}

	/**
	 * Non-blocking retrieval of the task result. If the result is available, the provided completion handler is called.
	 * This is only done once, so subsequent calls to this function won't trigger the handler again.
	 * <p>
	 * This is a convenience function similar to:
	 * <pre>
	 * if (isDone() && [result_not_retrieved_before]) {
	 *     R result = get();
	 *     completionHandler.run(result);
	 * }
	 * </pre>
	 *
	 * @return true if result of the task has been retrieved.
	 */
	public boolean poll(Consumer<R> completionHandler) {
		if (state.get() == State.DONE) {
			if (resultAvailable.compareAndSet(true, false)) {
				R r = get();
				if (r != null) {
					completionHandler.accept(r);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Non-blocking retrieval of the task result. Throws an exception if the result is not available yet, or has been
	 * retrieved already.
	 * <p>
	 * To prevent this from happening, callers should use {@link AsyncTask#isDone()} before calling this function.
	 *
	 * @throws IllegalStateException if no valid result is available.
	 */
	public R get() {
		if (state.get() != State.DONE) {
			throw new IllegalStateException("Illegal state!");
		}
		state.compareAndSet(State.DONE, State.READY);
		resultAvailable.compareAndSet(true, false);
		return result.get();
	}

	/**
	 * Queue the task for execution by the given {@link ExecutorService}.
	 *
	 * @throws IllegalStateException if the task is not ready yet, after it has been scheduled previously.
	 */
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

	/**
	 * Called by {@link FutureTask#done()} in scope of executor thread.
	 */
	public void done() {

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

				AsyncTask.this.done();

			} catch (InterruptedException | ExecutionException e) {
				throw new IllegalStateException(e);
			}

		}
	}
}
