package threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {
    static final class SleepTask implements Callable<String> {
        private final int id;
        private final long ms;

        SleepTask(int id, long ms) {
            this.id = id;
            this.ms = ms;
        }

        @Override
        public String call() throws Exception {
            System.out.println("[Task] #" + id + " started, sleeping " + ms + "ms");
            Thread.sleep(ms);
            System.out.println("[Task] #" + id + " finished");
            return "ok-" + id;
        }

        @Override
        public String toString() {
            return "SleepTask{" + id + "}";
        }
    }

    public static void main(String[] args) throws Exception {
        MyThreadPool pool = new MyThreadPool(
                "MyPool",
                2,
                4,
                5,
                TimeUnit.SECONDS,
                5,
                2
        );

        List<Future<String>> futures = new ArrayList<>();

        // часть задач - нормальная нагрузка
        for (int i = 1; i <= 8; i++) {
            try {
                futures.add(pool.submit(new SleepTask(i, 800)));
            } catch (Exception e) {
                System.out.println("[Main] submit rejected for task " + i + ": " + e.getMessage());
            }
        }

        // перегрузка: много задач сразу
        for (int i = 9; i <= 25; i++) {
            try {
                futures.add(pool.submit(new SleepTask(i, 300)));
            } catch (Exception e) {
                System.out.println("[Main] submit rejected for task " + i + ": " + e.getMessage());
            }
        }

        Thread.sleep(2000);
        pool.shutdown();

        for (Future<String> f : futures) {
            try {
                System.out.println("[Main] result: " + f.get(10, TimeUnit.SECONDS));
            } catch (Exception ignored) {
            }
        }

        System.out.println("[Main] done.");
    }
}
