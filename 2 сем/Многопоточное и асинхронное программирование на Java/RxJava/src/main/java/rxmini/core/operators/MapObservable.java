package rxmini.core.operators;

import rxmini.core.Disposable;
import rxmini.core.Observable;
import rxmini.core.Observer;

import java.util.function.Function;

public final class MapObservable {

    private MapObservable() {}

    public static <T, R> Observable<R> wrap(Observable<T> upstream, Function<? super T, ? extends R> mapper) {
        return observer -> upstream.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                R mapped;
                try {
                    mapped = mapper.apply(item);
                } catch (Throwable e) {
                    observer.onError(e);
                    return;
                }
                observer.onNext(mapped);
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        });
    }
}
