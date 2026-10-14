package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/** Starts JavaFX once for UI tests and runs assertions on its application thread. */
class JavaFxTestUtil {
    private static boolean started;

    /** Starts the JavaFX toolkit if a display is available. */
    static synchronized void startJavaFx() throws Exception {
        assumeTrue(!System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null,
                "JavaFX UI tests require a display (use xvfb-run on headless Linux)");
        if (started) {
            return;
        }
        CountDownLatch startup = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            startup.countDown();
        });
        assertTrue(startup.await(10, TimeUnit.SECONDS));
        started = true;
    }

    /** Runs test assertions on the JavaFX application thread. */
    static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
