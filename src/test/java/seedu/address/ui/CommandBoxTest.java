package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;

public class CommandBoxTest {

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
    public void commandEntry_emptyInputDoesNotExecuteAndSuccessClearsInput() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger executionCount = new AtomicInteger();
            AtomicReference<String> executedText = new AtomicReference<>();
            CommandBox commandBox = new CommandBox(commandText -> {
                executionCount.incrementAndGet();
                executedText.set(commandText);
                return new CommandResult("Done");
            });
            TextField input = (TextField) commandBox.getRoot().lookup("#commandTextField");

            input.fireEvent(new ActionEvent());
            assertEquals(0, executionCount.get());

            input.setText("help");
            input.fireEvent(new ActionEvent());

            assertEquals(1, executionCount.get());
            assertEquals("help", executedText.get());
            assertEquals("", input.getText());
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void commandEntry_failureRetainsInputAndEditingRestoresDefaultStyle() throws Exception {
        runOnFxThread(() -> {
            CommandBox commandBox = new CommandBox(commandText -> {
                throw new ParseException("Invalid command");
            });
            TextField input = (TextField) commandBox.getRoot().lookup("#commandTextField");
            input.setText("invalid");

            input.fireEvent(new ActionEvent());

            assertEquals("invalid", input.getText());
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            input.setText("corrected");
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
