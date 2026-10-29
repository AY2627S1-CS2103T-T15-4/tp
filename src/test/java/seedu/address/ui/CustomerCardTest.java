package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.customer.Customer;

public class CustomerCardTest {

    private static final Customer ALEX = new Customer(7, "Alex Tan", "91234567", "alex@example.com", "98765432");

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
    public void customerCard_displaysPersistentIdAndAllFields() throws Exception {
        runOnFxThread(() -> {
            Region card = new CustomerCard(ALEX).getRoot();

            assertEquals("7. ", label(card, "id"));
            assertEquals("Alex Tan", label(card, "name"));
            assertEquals("91234567", label(card, "phone"));
            assertEquals("alex@example.com", label(card, "email"));
            assertEquals("Backup Contact:", label(card, "backupContactLabel"));
            assertEquals("98765432", label(card, "backupContact"));
            assertEquals("None", label(card, "dogIds"));
            assertTrue(card.lookup("#phoneIcon") instanceof Region);
            assertTrue(card.lookup("#emailIcon") instanceof Region);
            assertTrue(((Label) card.lookup("#name")).isWrapText());
            assertEquals(1, ((Label) card.lookup("#name")).getLineSpacing());
            assertEquals("91234567", ((Label) card.lookup("#phone")).getTooltip().getText());
            assertEquals("alex@example.com", ((Label) card.lookup("#email")).getTooltip().getText());
            assertTrue(card.lookup("#backupContactLabel").getStyleClass().contains("backup-contact-label"));
            assertTrue(card.lookup("#phone").getParent() instanceof HBox);

            HBox nameRow = (HBox) card.lookup("#name").getParent();
            assertEquals(Pos.TOP_LEFT, nameRow.getAlignment());
            assertEquals(2, ((VBox) nameRow.getParent()).getSpacing());

            Scene scene = new Scene(card, 300, 250);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            scene.getRoot().applyCss();
            Region dogIcon = (Region) card.lookup("#dogIcon");
            assertTrue(dogIcon.getStyleClass().contains("dog-icon"));
            assertNotNull(dogIcon.getShape());
        });
    }

    private static String label(Region card, String id) {
        return ((Label) card.lookup("#" + id)).getText();
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
