package rxmini.core.schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SingleThreadScheduler implements Scheduler {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
}
