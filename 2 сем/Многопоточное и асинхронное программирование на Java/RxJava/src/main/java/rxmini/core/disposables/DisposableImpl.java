package rxmini.core.disposables;

import rxmini.core.Disposable;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DisposableImpl implements Disposable {
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    @Override
    public void dispose() {
        disposed.set(true);
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }
}
