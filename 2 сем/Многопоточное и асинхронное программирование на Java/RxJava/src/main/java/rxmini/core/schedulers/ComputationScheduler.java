package rxmini.core.schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ComputationScheduler implements Scheduler {
    private final ExecutorService executor;

    public ComputationScheduler(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public ComputationScheduler() {
        this(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
}
