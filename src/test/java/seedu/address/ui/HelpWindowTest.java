package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.stage.Stage;

public class HelpWindowTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        assumeTrue(!System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null,
                "JavaFX UI tests require a display (use xvfb-run on headless Linux)");
        try {
            CountDownLatch started = new CountDownLatch(1);
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                started.countDown();
            });
            assertTrue(started.await(10, TimeUnit.SECONDS));
        } catch (IllegalStateException alreadyStarted) {
            Platform.setImplicitExit(false);
        }
    }

    @Test
    public void focus_minimizedWindow_restoresWindow() throws Exception {
        HelpWindow window = runOnFxThread(() -> new HelpWindow(new Stage()));
        try {
            runOnFxThread(() -> {
                window.show();
                return null;
            });
            awaitState(() -> window.isShowing() && window.getRoot().isFocused(), "Help window shown and focused");
            runOnFxThread(() -> {
                window.getRoot().setIconified(true);
                return null;
            });
            awaitState(() -> window.isShowing() && window.getRoot().isIconified(), "Help window minimized");
            runOnFxThread(() -> {
                window.focus();
                return null;
            });
            awaitState(() -> window.isShowing() && !window.getRoot().isIconified()
                    && window.getRoot().isFocused(), "Help window restored and focused");
            runOnFxThread(() -> {
                window.focus();
                return null;
            });
            awaitState(() -> window.isShowing() && !window.getRoot().isIconified(), "Help window remains restored");
        } finally {
            runOnFxThread(() -> {
                window.hide();
                return null;
            });
        }
    }

    private static void awaitState(Callable<Boolean> condition, String description) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (runOnFxThread(condition)) {
                return;
            }
            // Wait on the test thread so native window events can run on the FX thread.
            Thread.sleep(25);
        }
        assertTrue(runOnFxThread(condition), "Timed out waiting for: " + description);
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
