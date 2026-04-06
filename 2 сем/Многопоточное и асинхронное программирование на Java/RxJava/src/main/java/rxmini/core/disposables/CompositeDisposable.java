package rxmini.core.disposables;

import rxmini.core.Disposable;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CompositeDisposable implements Disposable {
    private final CopyOnWriteArrayList<Disposable> disposables = new CopyOnWriteArrayList<>();
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    public void add(Disposable d) {
        Objects.requireNonNull(d);
        if (disposed.get()) {
            d.dispose();
            return;
        }
        disposables.add(d);
    }

    public void addAll(Collection<? extends Disposable> ds) {
        for (Disposable d : ds) add(d);
    }

    @Override
    public void dispose() {
        if (disposed.compareAndSet(false, true)) {
            for (Disposable d : disposables) {
                d.dispose();
            }
            disposables.clear();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }
}
