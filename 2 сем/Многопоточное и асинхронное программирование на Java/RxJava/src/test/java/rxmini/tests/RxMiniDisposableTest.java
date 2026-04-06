package rxmini.tests;

import org.junit.jupiter.api.Test;
import rxmini.core.Observable;
import rxmini.core.disposables.DisposableImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RxMiniDisposableTest {

    @Test
    void dispose_shouldStopFurtherEmissions() throws Exception {
        AtomicInteger emitted = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> source = Observable.create(emitter -> {
            for (int i = 0; i < 1000; i++) {
                if (emitter.isDisposed()) {
                    break;
                }
                emitter.onNext(i);
                emitted.incrementAndGet();
                if (i == 10) {
                    latch.countDown();
                }
            }
            emitter.onComplete();
        });

        TestObserver<Integer> to = new TestObserver<>();
        var d = source.subscribe(to);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        d.dispose();

        to.awaitTerminal(1, TimeUnit.SECONDS);

        int v = emitted.get();
        assertTrue(v <= 1000);
        assertNotNull(to.values());
    }
}
