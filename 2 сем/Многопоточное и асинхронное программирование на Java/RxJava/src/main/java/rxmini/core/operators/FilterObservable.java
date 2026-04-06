package rxmini.core.operators;

import rxmini.core.Observable;
import rxmini.core.Observer;

import java.util.function.Predicate;

public final class FilterObservable {

    private FilterObservable() {}

    public static <T> Observable<T> wrap(Observable<T> upstream, Predicate<? super T> predicate) {
        return observer -> upstream.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                boolean pass;
                try {
                    pass = predicate.test(item);
                } catch (Throwable e) {
                    observer.onError(e);
                    return;
                }
                if (pass) observer.onNext(item);
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
