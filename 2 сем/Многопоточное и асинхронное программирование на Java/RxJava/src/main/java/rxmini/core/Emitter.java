package rxmini.core;

public interface Emitter<T> {
    void onNext(T item);
    void onError(Throwable t);
    void onComplete();

    void setDisposable(Disposable disposable);
    boolean isDisposed();
}
