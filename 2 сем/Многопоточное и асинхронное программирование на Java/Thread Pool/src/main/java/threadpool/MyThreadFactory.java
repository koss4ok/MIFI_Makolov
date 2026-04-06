package threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class MyThreadFactory implements ThreadFactory {
    private final String poolName;
    private final AtomicInteger idx = new AtomicInteger(0);

    MyThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = poolName + "-worker-" + idx.incrementAndGet();
        System.out.println("[ThreadFactory] Creating new thread: " + name);
        Thread t = new Thread(r);
        t.setName(name);
        return t;
    }
}
