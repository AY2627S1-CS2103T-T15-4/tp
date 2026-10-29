package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;

/** Reconciles card lists without rebuilding unchanged entries or losing the visible record. */
final class CardListUpdater {

    private CardListUpdater() {
    }

    /**
     * Applies {@code desiredEntries} incrementally and keeps the first visible record anchored when possible.
     */
    static void reconcile(ListView<Supplier<Region>> listView, List<CardListEntry> desiredEntries) {
        requireNonNull(listView);
        requireNonNull(desiredEntries);
        ObservableList<Supplier<Region>> currentEntries = listView.getItems();
        CardListEntry anchor = findFirstVisibleEntry(listView);
        int oldAnchorIndex = anchor == null ? -1 : indexOfKey(currentEntries, anchor.getKey(), 0);

        for (int index = 0; index < desiredEntries.size(); index++) {
            CardListEntry desired = desiredEntries.get(index);
            if (index < currentEntries.size() && hasKey(currentEntries.get(index), desired.getKey())) {
                CardListEntry current = (CardListEntry) currentEntries.get(index);
                if (!current.hasSameContent(desired)) {
                    currentEntries.set(index, desired);
                }
                continue;
            }

            int existingIndex = indexOfKey(currentEntries, desired.getKey(), index + 1);
            if (existingIndex >= 0) {
                CardListEntry existing = (CardListEntry) currentEntries.remove(existingIndex);
                currentEntries.add(index, existing.hasSameContent(desired) ? existing : desired);
            } else {
                currentEntries.add(index, desired);
            }
        }
        while (currentEntries.size() > desiredEntries.size()) {
            currentEntries.remove(currentEntries.size() - 1);
        }

        restoreAnchor(listView, anchor, oldAnchorIndex);
    }

    /** Scrolls the list to the beginning when a command intentionally replaces the displayed result set. */
    static void scrollToTop(ListView<Supplier<Region>> listView) {
        requireNonNull(listView);
        if (!listView.getItems().isEmpty()) {
            listView.scrollTo(0);
        }
    }

    /** Returns the first currently visible stable card entry, if the list has been rendered. */
    private static CardListEntry findFirstVisibleEntry(ListView<Supplier<Region>> listView) {
        return listView.lookupAll(".list-cell").stream()
                .filter(ListCell.class::isInstance)
                .map(ListCell.class::cast)
                .filter(cell -> cell.isVisible() && !cell.isEmpty() && cell.getIndex() >= 0)
                .min(Comparator.comparingInt(ListCell::getIndex))
                .map(ListCell::getItem)
                .filter(CardListEntry.class::isInstance)
                .map(CardListEntry.class::cast)
                .orElse(null);
    }

    /** Restores the previous anchor, or its nearest successor if the anchored record was removed. */
    private static void restoreAnchor(ListView<Supplier<Region>> listView, CardListEntry anchor, int oldAnchorIndex) {
        if (anchor == null || listView.getItems().isEmpty()) {
            return;
        }
        int newAnchorIndex = indexOfKey(listView.getItems(), anchor.getKey(), 0);
        if (newAnchorIndex < 0) {
            newAnchorIndex = Math.min(oldAnchorIndex, listView.getItems().size() - 1);
        }
        if (newAnchorIndex != oldAnchorIndex) {
            listView.scrollTo(newAnchorIndex);
        }
    }

    /** Returns the index of {@code key} at or after {@code startIndex}, or -1 if it is absent. */
    private static int indexOfKey(List<Supplier<Region>> entries, String key, int startIndex) {
        for (int index = startIndex; index < entries.size(); index++) {
            if (hasKey(entries.get(index), key)) {
                return index;
            }
        }
        return -1;
    }

    /** Returns true if {@code supplier} is a stable card entry with the requested key. */
    private static boolean hasKey(Supplier<Region> supplier, String key) {
        return supplier instanceof CardListEntry entry && entry.hasKey(key);
    }
}
