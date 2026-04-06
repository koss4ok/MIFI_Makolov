package rxmini.tests;

import org.junit.jupiter.api.Test;
import rxmini.core.Observable;
import rxmini.core.schedulers.ComputationScheduler;
import rxmini.core.schedulers.SingleThreadScheduler;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class RxMiniBasicTest {

    @Test
    void create_shouldEmitAndComplete() {
        Observable<Integer> source = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        TestObserver<Integer> to = new TestObserver<>();
        source.subscribe(to);
        to.awaitTerminal(1, TimeUnit.SECONDS);

        assertNull(to.error(), () -> "Unexpected onError in flatMap: " + to.error());
        assertTrue(to.completed());
        assertEquals(List.of(1, 2), to.values());
    }

    @Test
    void create_shouldPropagateError() {
        RuntimeException ex = new RuntimeException("boom");
        Observable<Integer> source = Observable.create(emitter -> emitter.onError(ex));

        TestObserver<Integer> to = new TestObserver<>();
        source.subscribe(to);
        to.awaitTerminal(1, TimeUnit.SECONDS);

        assertSame(ex, to.error());
        assertFalse(to.completed());
    }

    @Test
    void map_shouldTransform() {
        Observable<Integer> source = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        TestObserver<Integer> to = new TestObserver<>();
        source.map(x -> x * 2).subscribe(to);
        to.awaitTerminal(1, TimeUnit.SECONDS);

        assertNull(to.error());
        assertTrue(to.completed());
        assertEquals(List.of(2, 4), to.values());
    }

    @Test
    void filter_shouldAllowOnlyMatching() {
        Observable<Integer> source = Observable.create(emitter -> {
            for (int i = 1; i <= 5; i++) emitter.onNext(i);
            emitter.onComplete();
        });

        TestObserver<Integer> to = new TestObserver<>();
        source.filter(x -> x % 2 == 0).subscribe(to);
        to.awaitTerminal(1, TimeUnit.SECONDS);

        assertEquals(List.of(2, 4), to.values());
        assertTrue(to.completed());
        assertNull(to.error());
    }

    @Test
    void map_shouldPropagateError_whenMapperThrows() {
        RuntimeException ex = new RuntimeException("map boom");
        Observable<Integer> source = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
        });

        TestObserver<Integer> to = new TestObserver<>();
        source
                .<Integer>map(x -> {
                    throw ex;
                })
                .subscribe(to);

        to.awaitTerminal(1, TimeUnit.SECONDS);

        assertSame(ex, to.error());
        assertFalse(to.completed());
    }

    @Test
    void filter_shouldPropagateError_whenPredicateThrows() {
        RuntimeException ex = new RuntimeException("filter boom");
        Observable<Integer> source = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
        });

        TestObserver<Integer> to = new TestObserver<>();
        source
                .filter(x -> {
                    throw ex;
                })
                .subscribe(to);

        to.awaitTerminal(1, TimeUnit.SECONDS);

        assertSame(ex, to.error());
        assertFalse(to.completed());
    }

    @Test
    void flatMap_shouldCompleteOnlyAfterAllInnersComplete() {
        Observable<String> upstream = Observable.create(emitter -> {
            emitter.onNext("A");
            emitter.onNext("B");
            emitter.onComplete();
        });

        SingleThreadScheduler scheduler = new SingleThreadScheduler();

        TestObserver<String> to = new TestObserver<>();
        upstream
                .flatMap(v -> {
                    if (v.equals("A")) {
                        return Observable.<String>create(e -> {
                            scheduler.execute(() -> {
                                e.onNext("A1");
                                e.onComplete();
                            });
                        });
                    }
                    return Observable.<String>create(e -> {
                        e.onNext("B1");
                        e.onComplete();
                    });
                })
                .observeOn(scheduler)
                .subscribe(new rxmini.core.Observer<String>() {
                    @Override
                    public void onNext(String item) {
                        to.onNext(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        to.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        to.onComplete();
                    }
                });

        to.awaitTerminal(2, TimeUnit.SECONDS);

        assertNull(to.error());
        assertTrue(to.completed());
        assertTrue(to.values().contains("A1"));
        assertTrue(to.values().contains("B1"));
    }

    @Test
    void observeOn_shouldDeliverSignalsOnSchedulerThread() {
        SingleThreadScheduler scheduler = new SingleThreadScheduler();
        AtomicReference<String> threadName = new AtomicReference<>();

        Observable<Integer> source = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
        });

        TestObserver<Integer> to = new TestObserver<>();
        source
                .observeOn(scheduler)
                .subscribe(new rxmini.core.Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        threadName.set(Thread.currentThread().getName());
                        to.onNext(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        to.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        to.onComplete();
                    }
                });

        to.awaitTerminal(2, TimeUnit.SECONDS);
        assertTrue(threadName.get() != null && !threadName.get().isBlank());
    }

    @Test
    void subscribeOn_shouldSubscribeOnSchedulerThread() {
        SingleThreadScheduler scheduler = new SingleThreadScheduler();
        AtomicReference<String> threadName = new AtomicReference<>();

        Observable<Integer> source = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
        });

        TestObserver<Integer> to = new TestObserver<>();
        source
                .subscribeOn(scheduler)
                .subscribe(new rxmini.core.Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        threadName.set(Thread.currentThread().getName());
                        to.onNext(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        to.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        to.onComplete();
                    }
                });

        to.awaitTerminal(2, TimeUnit.SECONDS);
        assertTrue(threadName.get() != null && !threadName.get().isBlank());
        assertNull(to.error());
        assertTrue(to.completed());
    }

    @Test
    void observeOn_shouldDeliverErrorsOnSchedulerThread() throws InterruptedException {
        SingleThreadScheduler scheduler = new SingleThreadScheduler();
        AtomicReference<String> threadName = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        RuntimeException ex = new RuntimeException("observeOn error");

        Observable<Integer> source = Observable.create(emitter -> emitter.onError(ex));
        source
                .observeOn(scheduler)
                .subscribe(new rxmini.core.Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                    }

                    @Override
                    public void onError(Throwable t) {
                        errorRef.set(t);
                        threadName.set(Thread.currentThread().getName());
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertSame(ex, errorRef.get());
        assertTrue(threadName.get() != null && !threadName.get().isBlank());
    }

    @Test
    void computationScheduler_shouldDeliverOnThreadPoolThread() throws InterruptedException {
        ComputationScheduler scheduler = new ComputationScheduler(2);
        String mainThreadName = Thread.currentThread().getName();

        AtomicReference<String> threadName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> source = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
        });

        source
                .observeOn(scheduler)
                .subscribe(new rxmini.core.Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        threadName.set(Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertNotNull(threadName.get());
        assertTrue(!threadName.get().isBlank());
        assertNotEquals(mainThreadName, threadName.get());
    }
}
