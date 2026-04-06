package threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class MyThreadPool implements CustomExecutor {
    private final String poolName;

    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit keepAliveUnit;
    private final int queueSize;
    private final int minSpareThreads;

    private final MyThreadFactory threadFactory;

    private final BlockingQueue<FutureTask<?>>[] queues;
    private final List<WorkerState<?>> workers = new ArrayList<>();

    private final AtomicInteger threadCount = new AtomicInteger(0);
    private final AtomicInteger rr = new AtomicInteger(0);

    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicBoolean shutdownNow = new AtomicBoolean(false);

    private final AtomicLong rejected = new AtomicLong(0);
    private final AtomicLong executed = new AtomicLong(0);

    private static final class WorkerState<T> {
        final BlockingQueue<FutureTask<T>> queue;
        final AtomicBoolean terminated;
        final String name;
        Thread thread;

        WorkerState(BlockingQueue<FutureTask<T>> queue, AtomicBoolean terminated, String name) {
            this.queue = queue;
            this.terminated = terminated;
            this.name = name;
        }
    }

    @SuppressWarnings("unchecked")
    public MyThreadPool(String poolName,
                         int corePoolSize,
                         int maxPoolSize,
                         long keepAliveTime,
                         TimeUnit keepAliveUnit,
                         int queueSize,
                         int minSpareThreads) {
        this.poolName = Objects.requireNonNull(poolName);
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveUnit = Objects.requireNonNull(keepAliveUnit);
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;

        if (corePoolSize <= 0 || maxPoolSize < corePoolSize) {
            throw new IllegalArgumentException("Invalid pool sizes");
        }
        if (minSpareThreads < 0 || minSpareThreads > maxPoolSize) {
            throw new IllegalArgumentException("Invalid minSpareThreads");
        }

        this.threadFactory = new MyThreadFactory(poolName);

        this.queues = (BlockingQueue<FutureTask<?>>[]) new BlockingQueue<?>[maxPoolSize];
        for (int i = 0; i < maxPoolSize; i++) {
            this.queues[i] = new java.util.concurrent.LinkedBlockingQueue<>(queueSize);
        }

        int initial = Math.max(corePoolSize, minSpareThreads);
        for (int i = 0; i < initial; i++) {
            startWorkerAtIndex(i);
        }
    }

    @Override
    public void execute(Runnable command) {
        Objects.requireNonNull(command);
        submitInternal(command, "Runnable");
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        Objects.requireNonNull(callable);
        TaskWrapper<T> wrapper = new TaskWrapper<>(callable, "Callable");
        return submitTask(wrapper);
    }

    private <T> Future<T> submitTask(TaskWrapper<T> wrapper) {
        if (shutdown.get()) {
            throw new RejectedExecutionException("Pool is shutdown");
        }
        ensureMinSpareThreads();

        long taskId = System.nanoTime();
        int qIndex = pickQueueIndex();
        @SuppressWarnings("unchecked")
        BlockingQueue<FutureTask<T>> q = (BlockingQueue<FutureTask<T>>) (BlockingQueue<?>) queues[qIndex];

        String desc = wrapper.description();
        FutureTask<T> ft = wrapper.task();

        boolean offered = q.offer(ft);
        if (!offered) {
            rejected.incrementAndGet();
            MyRejectedExecutionHandler.reject(taskId, desc);
        }

        System.out.println("[Pool] Task accepted into queue #" + qIndex + ": <" + desc + ">;");
        return ft;
    }

    private void submitInternal(Runnable command, String description) {
        TaskWrapper<Void> wrapper = new TaskWrapper<>(() -> {
            command.run();
            return null;
        }, description);
        submitTask(wrapper);
    }

    private int pickQueueIndex() {
        int currentThreads = threadCount.get();
        int size = Math.max(1, currentThreads);
        int idx = Math.floorMod(rr.getAndIncrement(), size);
        return idx;
    }

    private void ensureMinSpareThreads() {
        int current = threadCount.get();
        if (current < minSpareThreads) {
            int toStart = Math.min(maxPoolSize, minSpareThreads) - current;
            for (int i = 0; i < toStart; i++) {
                startWorkerAtIndex(current + i);
            }
        }
    }

    private void startWorkerAtIndex(int index) {
        if (index >= maxPoolSize) return;
        if (index < workers.size()) return;

        @SuppressWarnings("unchecked")
        BlockingQueue<FutureTask<Object>> q = (BlockingQueue<FutureTask<Object>>) (BlockingQueue<?>) queues[index];

        AtomicBoolean terminated = new AtomicBoolean(false);
        String name = poolName + "-worker-" + (index + 1);
        WorkerState<Object> st = new WorkerState<>(q, terminated, name);
        workers.add(st);

        Thread t = threadFactory.newThread(new Worker<>(q, shutdown, terminated, keepAliveUnit.toNanos(keepAliveTime), name, executed));
        st.thread = t;
        threadCount.incrementAndGet();
        t.start();
    }

    @Override
    public void shutdown() {
        shutdown.set(true);
        System.out.println("[Pool] shutdown() called.");
    }

    @Override
    public void shutdownNow() {
        shutdownNow.set(true);
        shutdown.set(true);
        System.out.println("[Pool] shutdownNow() called.");
        for (WorkerState<?> st : workers) {
            if (st.thread != null) {
                st.thread.interrupt();
            }
        }
        // queues will drain as workers stop
    }
}
