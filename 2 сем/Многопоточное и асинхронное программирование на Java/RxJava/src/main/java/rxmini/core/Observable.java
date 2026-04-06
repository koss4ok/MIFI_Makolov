package rxmini.core;

import rxmini.core.disposables.DisposableImpl;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface Observable<T> {

    Disposable subscribe(Observer<? super T> observer);

    default Disposable subscribe(Consumer<? super T> onNext,
                                 Consumer<? super Throwable> onError,
                                 Runnable onComplete) {
        Objects.requireNonNull(onNext);
        Objects.requireNonNull(onError);
        Objects.requireNonNull(onComplete);
        return subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                onNext.accept(item);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept(t);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        });
    }

    @SuppressWarnings("unchecked")
    static <T> Observable<T> create(Consumer<Emitter<T>> onSubscribe) {
        Objects.requireNonNull(onSubscribe);
        return observer -> {
            DisposableImpl d = new DisposableImpl();
            AtomicBoolean terminated = new AtomicBoolean(false);

            Emitter<T> emitter = new Emitter<T>() {
                @Override
                public void onNext(T item) {
                    if (d.isDisposed()) return;
                    if (terminated.get()) return;
                    observer.onNext(item);
                }

                @Override
                public void onError(Throwable t) {
                    if (d.isDisposed()) return;
                    if (terminated.compareAndSet(false, true)) {
                        try {
                            observer.onError(t);
                        } finally {
                            d.dispose();
                        }
                    }
                }

                @Override
                public void onComplete() {
                    if (d.isDisposed()) return;
                    if (terminated.compareAndSet(false, true)) {
                        try {
                            observer.onComplete();
                        } finally {
                            d.dispose();
                        }
                    }
                }

                @Override
                public void setDisposable(Disposable disposable) {
                }

                @Override
                public boolean isDisposed() {
                    return d.isDisposed();
                }
            };

            try {
                onSubscribe.accept(emitter);
            } catch (Throwable ex) {
                emitter.onError(ex);
            }

            return d;
        };
    }

    default <R> Observable<R> map(java.util.function.Function<? super T, ? extends R> mapper) {
        return rxmini.core.operators.MapObservable.wrap(this, mapper);
    }

    default Observable<T> filter(java.util.function.Predicate<? super T> predicate) {
        return rxmini.core.operators.FilterObservable.wrap(this, predicate);
    }

    default <R> Observable<R> flatMap(java.util.function.Function<? super T, ? extends Observable<? extends R>> mapper) {
        return rxmini.core.operators.FlatMapObservable.wrap(this, mapper);
    }

    default Observable<T> subscribeOn(rxmini.core.schedulers.Scheduler scheduler) {
        return rxmini.core.operators.SubscribeOnObservable.wrap(this, scheduler);
    }

    default Observable<T> observeOn(rxmini.core.schedulers.Scheduler scheduler) {
        return rxmini.core.operators.ObserveOnObservable.wrap(this, scheduler);
    }
}
