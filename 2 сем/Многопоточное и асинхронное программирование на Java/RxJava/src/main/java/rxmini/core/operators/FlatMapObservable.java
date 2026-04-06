package rxmini.core.operators;

import rxmini.core.Disposable;
import rxmini.core.Observable;
import rxmini.core.Observer;
import rxmini.core.disposables.CompositeDisposable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class FlatMapObservable {

    private FlatMapObservable() {}

    public static <T, R> Observable<R> wrap(
            Observable<T> upstream,
            Function<? super T, ? extends Observable<? extends R>> mapper
    ) {
        Objects.requireNonNull(mapper);

        return observer -> {
            CompositeDisposable composite = new CompositeDisposable();
            AtomicInteger active = new AtomicInteger(0);
            AtomicBoolean upstreamDone = new AtomicBoolean(false);

            Disposable upstreamDisp = upstream.subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    final Observable<? extends R> inner;
                    try {
                        inner = mapper.apply(item);
                    } catch (Throwable e) {
                        observer.onError(e);
                        composite.dispose();
                        return;
                    }

                    active.incrementAndGet();
                    Disposable innerDisp = ((Observable<R>) inner).subscribe(new Observer<R>() {
                        @Override
                        public void onNext(R item) {
                            observer.onNext(item);
                        }

                        @Override
                        public void onError(Throwable t) {
                            observer.onError(t);
                            composite.dispose();
                        }

                        @Override
                        public void onComplete() {
                            if (active.decrementAndGet() == 0 && upstreamDone.get()) {
                                observer.onComplete();
                            }
                        }
                    });
                    composite.add(innerDisp);
                }

                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                    composite.dispose();
                }

                @Override
                public void onComplete() {
                    upstreamDone.set(true);
                    if (active.get() == 0) {
                        observer.onComplete();
                    }
                }
            });

            composite.add(upstreamDisp);
            return composite;
        };
    }
}
