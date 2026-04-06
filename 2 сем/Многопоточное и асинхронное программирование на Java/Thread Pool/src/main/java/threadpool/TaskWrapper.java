package threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

final class TaskWrapper<T> {
    private final FutureTask<T> task;
    private final String description;

    TaskWrapper(Callable<T> callable, String description) {
        this.task = new FutureTask<>(callable);
        this.description = description;
    }

    FutureTask<T> task() {
        return task;
    }

    String description() {
        return description;
    }

    Runnable asRunnable() {
        return task;
    }
}
