package rxmini.core.operators;

import rxmini.core.Disposable;
import rxmini.core.Observable;
import rxmini.core.Observer;
import rxmini.core.disposables.CompositeDisposable;
import rxmini.core.schedulers.Scheduler;

import java.util.Objects;

public final class ObserveOnObservable {

    private ObserveOnObservable() {}

    public static <T> Observable<T> wrap(Observable<T> upstream, Scheduler scheduler) {
        Objects.requireNonNull(scheduler);

        return observer -> {
            CompositeDisposable composite = new CompositeDisposable();

            Observer<T> scheduled = new Observer<T>() {
                @Override
                public void onNext(T item) {
                    scheduler.execute(() -> {
                        if (composite.isDisposed()) return;
                        observer.onNext(item);
                    });
                }

                @Override
                public void onError(Throwable t) {
                    scheduler.execute(() -> {
                        if (composite.isDisposed()) return;
                        try {
                            observer.onError(t);
                        } finally {
                            composite.dispose();
                        }
                    });
                }

                @Override
                public void onComplete() {
                    scheduler.execute(() -> {
                        if (composite.isDisposed()) return;
                        try {
                            observer.onComplete();
                        } finally {
                            composite.dispose();
                        }
                    });
                }
            };

            Disposable d = upstream.subscribe(scheduled);
            composite.add(d);
            return composite;
        };
    }
}
