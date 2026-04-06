package rxmini.core.operators;

import rxmini.core.Disposable;
import rxmini.core.Observable;
import rxmini.core.Observer;
import rxmini.core.disposables.CompositeDisposable;
import rxmini.core.schedulers.Scheduler;

import java.util.Objects;

public final class SubscribeOnObservable {
    private SubscribeOnObservable() {}

    public static <T> Observable<T> wrap(Observable<T> upstream, Scheduler scheduler) {
        Objects.requireNonNull(scheduler);
        return observer -> {
            CompositeDisposable composite = new CompositeDisposable();
            scheduler.execute(() -> {
                if (composite.isDisposed()) return;
                Disposable d = upstream.subscribe(observer);
                composite.add(d);
            });
            return composite;
        };
    }
}
