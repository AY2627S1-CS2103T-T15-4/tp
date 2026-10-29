package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;

/** Provides shared tooltip and clipboard interactions for record cards. */
final class CardInteractionUtil {

    private static final Duration TOOLTIP_SHOW_DELAY = Duration.millis(300);
    private static final Duration COPY_CONFIRMATION_DURATION = Duration.seconds(1.2);
    private static final double TOOLTIP_MAX_WIDTH = 320;
    private static final double COPY_CONFIRMATION_HORIZONTAL_OFFSET = 72;
    private static final double COPY_CONFIRMATION_VERTICAL_OFFSET = 8;
    private static final Object TOOLTIP_PROPERTY_KEY = new Object();
    private static final Object COPY_MENU_PROPERTY_KEY = new Object();
    private static final Object COPY_VALUE_PROPERTY_KEY = new Object();

    private CardInteractionUtil() {
    }

    /** Installs a wrapped full-value tooltip over the supplied detail row. */
    static void installFullValueTooltip(Node row, String fullValue) {
        requireNonNull(row);
        requireNonNull(fullValue);
        Tooltip tooltip = new Tooltip(fullValue);
        tooltip.setShowDelay(TOOLTIP_SHOW_DELAY);
        tooltip.setMaxWidth(TOOLTIP_MAX_WIDTH);
        tooltip.setWrapText(true);
        tooltip.getStyleClass().add("card-tooltip");
        Tooltip.install(row, tooltip);
        row.getProperties().put(TOOLTIP_PROPERTY_KEY, tooltip);
    }

    /** Returns the full-value tooltip installed on the supplied detail row. */
    static Tooltip getFullValueTooltip(Node row) {
        requireNonNull(row);
        return (Tooltip) row.getProperties().get(TOOLTIP_PROPERTY_KEY);
    }

    /** Creates a menu item that copies the supplied value and shows a brief confirmation. */
    static MenuItem createCopyMenuItem(String text, String value, Node confirmationAnchor,
            Consumer<String> clipboardWriter) {
        requireNonNull(text);
        requireNonNull(value);
        requireNonNull(confirmationAnchor);
        requireNonNull(clipboardWriter);
        MenuItem item = new MenuItem(text);
        item.getProperties().put(COPY_VALUE_PROPERTY_KEY, value);
        item.setOnAction(event -> {
            clipboardWriter.accept(value);
            showCopyConfirmation(confirmationAnchor);
        });
        return item;
    }

    /** Installs a context menu that opens when the user right-clicks anywhere on a card. */
    static void installCopyMenu(Node card, ContextMenu copyMenu) {
        requireNonNull(card);
        requireNonNull(copyMenu);
        card.setOnContextMenuRequested(event -> {
            copyMenu.show(card, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        card.getProperties().put(COPY_MENU_PROPERTY_KEY, copyMenu);
    }

    /** Returns the copy menu installed on the supplied card. */
    static ContextMenu getCopyMenu(Node card) {
        requireNonNull(card);
        return (ContextMenu) card.getProperties().get(COPY_MENU_PROPERTY_KEY);
    }

    /** Returns the plain-text value configured for the supplied copy menu item. */
    static String getCopyValue(MenuItem copyItem) {
        requireNonNull(copyItem);
        return (String) copyItem.getProperties().get(COPY_VALUE_PROPERTY_KEY);
    }

    /** Writes the supplied text to the operating system clipboard. */
    static void copyToSystemClipboard(String value) {
        requireNonNull(value);
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private static void showCopyConfirmation(Node anchor) {
        if (anchor.getScene() == null || anchor.getScene().getWindow() == null
                || !anchor.getScene().getWindow().isShowing()) {
            return;
        }
        Bounds screenBounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (screenBounds == null) {
            return;
        }

        Tooltip confirmation = new Tooltip("Copied!");
        confirmation.getStyleClass().add("copy-confirmation");
        confirmation.show(anchor,
                screenBounds.getMaxX() - COPY_CONFIRMATION_HORIZONTAL_OFFSET,
                screenBounds.getMinY() + COPY_CONFIRMATION_VERTICAL_OFFSET);

        PauseTransition hideDelay = new PauseTransition(COPY_CONFIRMATION_DURATION);
        hideDelay.setOnFinished(event -> confirmation.hide());
        hideDelay.play();
    }
}
