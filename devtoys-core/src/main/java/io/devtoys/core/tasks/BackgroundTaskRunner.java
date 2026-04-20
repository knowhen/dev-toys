package io.devtoys.core.tasks;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Helper that runs tool computations off the JavaFX Application Thread and
 * hands results back on the FX thread — with automatic cancellation of in-flight
 * work when a new request supersedes it.
 *
 * <p>Use pattern in a tool:
 * <pre>{@code
 * private final DebouncedTaskRunner<String> runner = new DebouncedTaskRunner<>();
 *
 * input.textProperty().addListener((obs, o, n) -> {
 *     runner.run(() -> expensiveCompute(n),   // runs on background thread
 *                output::setText,             // runs on FX thread with result
 *                err -> status.setText("Error: " + err.getMessage()));
 * });
 * }</pre>
 *
 * <p>Semantics:
 * <ul>
 *   <li>Only one task is "active" at a time per runner. Submitting a new one
 *       cancels the previous (its result is discarded).</li>
 *   <li>Results are delivered on the JavaFX Application Thread, always.</li>
 *   <li>A shared daemon pool backs all runners, sized at
 *       {@code max(2, availableProcessors())}, which is plenty because DevToys
 *       tools are latency-sensitive but not throughput-sensitive.</li>
 * </ul>
 *
 * <p>The {@link #busyProperty()} exposes whether there is currently a running
 * task, so UIs can show a progress indicator when it matters (mostly only for
 * image tools — text tools finish in milliseconds).
 */
public final class BackgroundTaskRunner {

    private static final Logger LOG = LoggerFactory.getLogger(BackgroundTaskRunner.class);

    private static final ExecutorService POOL = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()),
            new DaemonThreadFactory());

    private BackgroundTaskRunner() {}

    /** Call on app shutdown if you want a clean exit. JVM exit would also do it. */
    public static void shutdown() {
        POOL.shutdownNow();
    }

    /**
     * A per-tool runner that holds exactly one in-flight {@link Future} and
     * cancels it when a newer request arrives.
     *
     * @param <R> result type
     */
    public static final class DebouncedTaskRunner<R> {

        private final ReadOnlyBooleanWrapper busy = new ReadOnlyBooleanWrapper(false);
        private volatile Future<?> current;

        public ReadOnlyBooleanProperty busyProperty() {
            return busy.getReadOnlyProperty();
        }

        /**
         * Submits a new computation. Any previous pending computation is
         * cancelled.
         *
         * @param work      the computation, runs on a background thread
         * @param onSuccess invoked on the FX thread with the result
         * @param onError   invoked on the FX thread if {@code work} throws
         */
        public void run(Supplier<R> work, Consumer<R> onSuccess, Consumer<Throwable> onError) {
            Future<?> prev = current;
            if (prev != null && !prev.isDone()) {
                prev.cancel(true);
            }
            setBusy(true);
            current = POOL.submit(() -> {
                try {
                    R result = work.get();
                    if (!Thread.currentThread().isInterrupted()) {
                        Platform.runLater(() -> {
                            try { onSuccess.accept(result); }
                            finally { setBusy(false); }
                        });
                    } else {
                        Platform.runLater(() -> setBusy(false));
                    }
                } catch (RuntimeException e) {
                    if (Thread.currentThread().isInterrupted()) {
                        Platform.runLater(() -> setBusy(false));
                        return;
                    }
                    LOG.debug("Task failed", e);
                    Platform.runLater(() -> {
                        try { onError.accept(e); }
                        finally { setBusy(false); }
                    });
                }
            });
        }

        private void setBusy(boolean b) {
            if (Platform.isFxApplicationThread()) {
                busy.set(b);
            } else {
                Platform.runLater(() -> busy.set(b));
            }
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "devtoys-bg-" + counter.getAndIncrement());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        }
    }
}
