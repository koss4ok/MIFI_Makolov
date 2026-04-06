package threadpool;

import java.util.concurrent.RejectedExecutionException;

final class MyRejectedExecutionHandler {
    static <T> void reject(long taskId, String description) {
        System.out.println("[Rejected] Task #" + taskId + " was rejected due to overload: " + description);
        throw new RejectedExecutionException("Overload: task #" + taskId + " rejected");
    }

    private MyRejectedExecutionHandler() {}
}
