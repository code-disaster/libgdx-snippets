package com.badlogic.gdx.concurrent;

import com.badlogic.gdx.function.Consumer;
import com.badlogic.gdx.function.Predicate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A reentrant wrapper to {@link FutureTask}, with some additional properties to synchronize its result with the
 * calling thread.
 * <p>
 * The task is (re-)scheduled for asynchronous execution with {@link AsyncTask#execute(ExecutorService)}.
 * <p>
 * Upon completion, a {@link CyclicBarrier} is entered, waiting for the scheduling thread to call
 * {@link AsyncTask#await(Consumer)}. After both threads have entered the barrier, {@link AsyncTaskJob#completed()}
 * is called.
 */
public class AsyncTask<V extends AsyncTaskJob<V>> {

	private enum State {
		READY,
		PENDING,
		COMPLETED
	}

	protected final V job;

	private final CyclicBarrier completionBarrier;

	private final AtomicReference<State> state = new AtomicReference<>(State.READY);

	private Task task;

	public AsyncTask(V job) {
		this.job = job;
		completionBarrier = new CyclicBarrier(2, this::completed);
	}

	public boolean consumeJobPredicate(Predicate<V> consumer) {

		if (state.get() != State.READY) {
			//throw new IllegalStateException("Invalid task state!");
			return false;
		}

		return consumer.test(job);
	}

	public void consumeJob(Consumer<V> consumer) {

		if (state.get() != State.READY) {
			//throw new IllegalStateException("Invalid task state!");
			return;
		}

		consumer.accept(job);
	}

	public boolean isReady() {
		return state.get() == State.READY;
	}

	public boolean isPending() {
		return state.get() == State.PENDING;
	}

	public boolean isCompleted() {
		return state.get() == State.COMPLETED;
	}

	/**
	 * Enters the task's completion barrier, waiting for {@link AsyncTaskJob#completed()} to be called.
	 *
	 * This function blocks execution if the task is still pending. Use {@link AsyncTask#isCompleted()}
	 * for a non-blocking check.
	 *
	 * Returns the arrival index of the current thread, see {@link CyclicBarrier#await()}.
	 */
	public int await(Consumer<V> consumeAfterCompletion) throws InterruptedException {

		if (state.get() == State.READY) {
			throw new IllegalStateException("Invalid task state!");
		}

		try {

			int arrivalIndex = completionBarrier.await();

			task.get(); // this causes an ExecutionException if there has been some error

			if (consumeAfterCompletion != null) {
				consumeAfterCompletion.accept(job);
			}

			if (!state.compareAndSet(State.COMPLETED, State.READY)) {
				throw new IllegalStateException("Invalid task state!");
			}

			return arrivalIndex;

		} catch (BrokenBarrierException e) {
			throw new InterruptedException(e.getMessage());
		} catch (ExecutionException e) {
			throw new RuntimeException("Exception thrown during execution of asynchronous task!", e);
		}
	}

	/**
	 * Queue the task for execution by the given {@link ExecutorService}.
	 *
	 * @throws IllegalStateException if the task is not ready yet, after it has been scheduled previously.
	 */
	void execute(ExecutorService service) {

		// reset state
		if (!state.compareAndSet(State.READY, State.PENDING)) {
			throw new IllegalStateException("Invalid task state!");
		}

		completionBarrier.reset();

		// pass to executor service
		service.execute(task = new Task());
	}

	/**
	 * Called from {@link CyclicBarrier} when the async task is completed.
	 */
	private void completed() {
		job.completed();
	}

	private class Task extends FutureTask<V> {

		Task() {
			super(job);
		}

		@Override
		protected void done() {

			try {

				if (!state.compareAndSet(State.PENDING, State.COMPLETED)) {
					throw new IllegalStateException("Invalid completion state!");
				}

				completionBarrier.await();

			} catch (InterruptedException | BrokenBarrierException e) {
				throw new IllegalStateException(e);
			}

		}
	}
}
