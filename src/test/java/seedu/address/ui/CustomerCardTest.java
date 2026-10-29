package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
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
            Tooltip phoneTooltip = CardInteractionUtil.getFullValueTooltip(card.lookup("#phoneRow"));
            Tooltip emailTooltip = CardInteractionUtil.getFullValueTooltip(card.lookup("#emailRow"));
            assertEquals("91234567", phoneTooltip.getText());
            assertEquals("alex@example.com", emailTooltip.getText());
            assertEquals(Duration.millis(300), emailTooltip.getShowDelay());
            assertEquals(320, emailTooltip.getMaxWidth());
            assertTrue(emailTooltip.isWrapText());
            assertTrue(emailTooltip.getStyleClass().contains("card-tooltip"));
            assertTrue(card.lookup("#backupContactLabel").getStyleClass().contains("backup-contact-label"));
            assertTrue(card.lookup("#phone").getParent() instanceof HBox);

            HBox nameRow = (HBox) card.lookup("#name").getParent();
            assertEquals(Pos.TOP_LEFT, nameRow.getAlignment());
            assertEquals(2, ((VBox) nameRow.getParent()).getSpacing());
            assertEquals(new Insets(10, 14, 10, 14), ((VBox) nameRow.getParent()).getPadding());

            Scene scene = new Scene(card, 300, 250);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            scene.getRoot().applyCss();
            Region dogIcon = (Region) card.lookup("#dogIcon");
            assertTrue(dogIcon.getStyleClass().contains("dog-icon"));
            assertNotNull(dogIcon.getShape());
        });
    }

    @Test
    public void copyMenu_copiesIndividualAndLabelledCustomerDetails() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<String> copiedText = new AtomicReference<>();
            CustomerCard customerCard = new CustomerCard(ALEX, List.of(4, 2), recordingWriter(copiedText));
            Region card = customerCard.getRoot();
            ContextMenu copyMenu = CardInteractionUtil.getCopyMenu(card);

            assertNotNull(card.getOnContextMenuRequested());
            assertEquals(5, copyMenu.getItems().size());
            assertEquals("Copy phone number", copyMenu.getItems().get(0).getText());
            assertEquals("Copy email address", copyMenu.getItems().get(1).getText());
            assertEquals("Copy backup contact", copyMenu.getItems().get(2).getText());
            assertTrue(copyMenu.getItems().get(3).getStyleClass().contains("separator-menu-item"));
            assertEquals("Copy all customer details", copyMenu.getItems().get(4).getText());

            copyMenu.getItems().get(0).fire();
            assertEquals("91234567", copiedText.get());
            copyMenu.getItems().get(1).fire();
            assertEquals("alex@example.com", copiedText.get());
            copyMenu.getItems().get(2).fire();
            assertEquals("98765432", copiedText.get());
            copyMenu.getItems().get(4).fire();
            assertEquals(CardCopyFormatter.formatCustomer(ALEX, List.of(4, 2)), copiedText.get());
        });
    }

    @Test
    public void copyMenu_longValuesAndRepeatedCopies_preserveCompleteLatestValue() throws Exception {
        runOnFxThread(() -> {
            String longEmail = "Very.Long.Email.Address.With.Many.Parts@example-domain.com";
            Customer longValueCustomer = new Customer(8, "Casey Tan", "00123456", longEmail, "00987654");
            Customer otherCustomer = new Customer(9, "Beth Tan", "99887766", "Beth.Tan@example.com", "88776655");
            AtomicReference<String> copiedText = new AtomicReference<>();
            ContextMenu longValueMenu = CardInteractionUtil.getCopyMenu(
                    new CustomerCard(longValueCustomer, List.of(), recordingWriter(copiedText)).getRoot());
            ContextMenu otherMenu = CardInteractionUtil.getCopyMenu(
                    new CustomerCard(otherCustomer, List.of(), recordingWriter(copiedText)).getRoot());

            longValueMenu.getItems().get(0).fire();
            assertEquals("00123456", copiedText.get());
            longValueMenu.getItems().get(1).fire();
            assertEquals(longEmail, copiedText.get());
            longValueMenu.getItems().get(2).fire();
            assertEquals("00987654", copiedText.get());
            otherMenu.getItems().get(1).fire();
            assertEquals("Beth.Tan@example.com", copiedText.get());
            longValueMenu.getItems().get(4).fire();
            assertEquals(CardCopyFormatter.formatCustomer(longValueCustomer, List.of()), copiedText.get());
        });
    }

    @Test
    public void copyMenuItem_reportsClipboardSuccessAndFailureExactlyOnce() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger writeCount = new AtomicInteger();
            AtomicReference<String> copiedText = new AtomicReference<>();
            AtomicReference<Boolean> feedback = new AtomicReference<>();
            StackPane anchor = new StackPane();

            var successfulItem = CardInteractionUtil.createCopyMenuItem("Copy", "complete value", anchor,
                    value -> {
                        writeCount.incrementAndGet();
                        copiedText.set(value);
                        return true;
                    }, feedback::set);
            successfulItem.fire();

            assertEquals(1, writeCount.get());
            assertEquals("complete value", copiedText.get());
            assertEquals(Boolean.TRUE, feedback.get());
            assertEquals("Copied!", CardInteractionUtil.MESSAGE_COPY_SUCCESS);

            var failedItem = CardInteractionUtil.createCopyMenuItem("Copy", "failed value", anchor,
                    value -> {
                        writeCount.incrementAndGet();
                        copiedText.set(value);
                        return false;
                    }, feedback::set);
            failedItem.fire();

            assertEquals(2, writeCount.get());
            assertEquals("failed value", copiedText.get());
            assertEquals(Boolean.FALSE, feedback.get());
            assertEquals("Could not copy. Please try again.", CardInteractionUtil.MESSAGE_COPY_FAILURE);
        });
    }

    @Test
    public void contextMenuRequest_opensInstalledMenuAndConsumesEvent() throws Exception {
        runOnFxThread(() -> {
            Region card = new CustomerCard(ALEX).getRoot();
            StackPane root = new StackPane(card);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 300, 250));
            stage.show();
            try {
                ContextMenu copyMenu = CardInteractionUtil.getCopyMenu(card);
                ContextMenuEvent request = new ContextMenuEvent(ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                        10, 10, 100, 100, false, new PickResult(card, 10, 10));

                card.getOnContextMenuRequested().handle(request);

                assertTrue(request.isConsumed());
                assertTrue(copyMenu.isShowing());
                copyMenu.hide();
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void copyFeedback_handlesDetachedAnchorAndShowsSuccessOrFailureBesideVisibleCard() throws Exception {
        runOnFxThread(() -> {
            assertNull(CardInteractionUtil.showCopyFeedback(new StackPane(), true));

            StackPane card = new StackPane();
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(card), 300, 250));
            assertNull(CardInteractionUtil.showCopyFeedback(card, true));
            stage.show();
            try {
                Tooltip success = CardInteractionUtil.showCopyFeedback(card, true);
                assertNotNull(success);
                assertEquals(CardInteractionUtil.MESSAGE_COPY_SUCCESS, success.getText());
                assertTrue(success.isShowing());
                success.hide();

                Tooltip failure = CardInteractionUtil.showCopyFeedback(card, false);
                assertNotNull(failure);
                assertEquals(CardInteractionUtil.MESSAGE_COPY_FAILURE, failure.getText());
                assertTrue(failure.isShowing());
                failure.hide();
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void cardHover_reservesBorderSpaceForRegularAndTodayCards() throws Exception {
        runOnFxThread(() -> {
            assertHoverKeepsCardSize(false, Color.rgb(9, 107, 104, 0.65));
            assertHoverKeepsCardSize(true, Color.rgb(144, 209, 202, 0.85));
        });
    }

    private static void assertHoverKeepsCardSize(boolean todayCard, Color expectedHoverColor) {
        Region card = new CustomerCard(ALEX).getRoot();
        ListCell<Object> cell = new ListCell<>();
        cell.setGraphic(card);
        StackPane panel = new StackPane(cell);
        if (todayCard) {
            panel.getStyleClass().add("today-panel");
        }
        Scene scene = new Scene(panel, 300, 250);
        scene.getStylesheets().add(CustomerCardTest.class.getResource("/view/DarkTheme.css").toExternalForm());
        panel.applyCss();
        panel.layout();

        double normalHeight = card.prefHeight(300);
        var normalBorder = card.getBorder().getStrokes().get(0);
        assertEquals(Color.TRANSPARENT, normalBorder.getTopStroke());
        assertEquals(1, normalBorder.getWidths().getTop());

        cell.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        panel.applyCss();
        panel.layout();

        var hoverBorder = card.getBorder().getStrokes().get(0);
        assertEquals(expectedHoverColor, hoverBorder.getTopStroke());
        assertEquals(normalBorder.getWidths().getTop(), hoverBorder.getWidths().getTop());
        assertEquals(normalBorder.getWidths().getRight(), hoverBorder.getWidths().getRight());
        assertEquals(normalBorder.getWidths().getBottom(), hoverBorder.getWidths().getBottom());
        assertEquals(normalBorder.getWidths().getLeft(), hoverBorder.getWidths().getLeft());
        assertEquals(normalHeight, card.prefHeight(300), 0.01);
    }

    private static String label(Region card, String id) {
        return ((Label) card.lookup("#" + id)).getText();
    }

    private static ClipboardWriter recordingWriter(AtomicReference<String> copiedText) {
        return value -> {
            copiedText.set(value);
            return true;
        };
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
