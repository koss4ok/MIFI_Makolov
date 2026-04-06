package threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

final class Worker<T> implements Runnable {
    private final BlockingQueue<FutureTask<T>> queue;
    private final AtomicBoolean shutdown;
    private final AtomicBoolean terminated;
    private final long idleTimeoutNanos;
    private final String workerName;
    private final AtomicLong executedCount;

    Worker(BlockingQueue<FutureTask<T>> queue,
           AtomicBoolean shutdown,
           AtomicBoolean terminated,
           long idleTimeoutNanos,
           String workerName,
           AtomicLong executedCount) {
        this.queue = queue;
        this.shutdown = shutdown;
        this.terminated = terminated;
        this.idleTimeoutNanos = idleTimeoutNanos;
        this.workerName = workerName;
        this.executedCount = executedCount;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (shutdown.get() && queue.isEmpty()) {
                    return;
                }

                FutureTask<T> task = queue.poll(idleTimeoutNanos, TimeUnit.NANOSECONDS);
                if (task == null) {
                    if (shutdown.get() && queue.isEmpty()) return;
                    return; // idle timeout
                }

                executedCount.incrementAndGet();
                System.out.println("[Worker] " + workerName + " executes <" + describe(task) + ">");
                task.run();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            terminated.set(true);
            System.out.println("[Worker] " + workerName + " terminated.");
        }
    }

    private String describe(FutureTask<T> task) {
        // FutureTask doesn't expose description; we just return a stable placeholder.
        return "task-" + System.identityHashCode(task);
    }
}
