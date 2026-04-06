package rxmini.tests;

import rxmini.core.Disposable;
import rxmini.core.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TestObserver<T> implements Observer<T> {
    private final List<T> values = new ArrayList<>();
    private volatile Throwable error;
    private volatile boolean completed;
    private final CountDownLatch terminal = new CountDownLatch(1);
    private final AtomicBoolean terminated = new AtomicBoolean(false);

    @Override
    public void onNext(T item) {
        if (terminal.getCount() == 0) return;
        values.add(item);
    }

    @Override
    public void onError(Throwable t) {
        if (terminated.compareAndSet(false, true)) {
            error = t;
            completed = false;
            terminal.countDown();
        }
    }

    @Override
    public void onComplete() {
        if (terminated.compareAndSet(false, true)) {
            completed = true;
            terminal.countDown();
        }
    }

    public List<T> values() {
        return values;
    }

    public Throwable error() {
        return error;
    }

    public boolean completed() {
        return completed;
    }

    public void awaitTerminal(long timeout, TimeUnit unit) {
        try {
            terminal.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
