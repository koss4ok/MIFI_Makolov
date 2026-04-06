package rxmini.core.schedulers;

public interface Scheduler {
    void execute(Runnable task);
}
