package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import javafx.scene.layout.Region;

/** Supplies a card while retaining stable record identity and a comparable content snapshot. */
final class CardListEntry implements Supplier<Region> {

    private final String key;
    private final Object contentSnapshot;
    private final Supplier<Region> cardFactory;

    /** Creates an entry for one stable record identity and content version. */
    CardListEntry(String key, Object contentSnapshot, Supplier<Region> cardFactory) {
        this.key = requireNonNull(key);
        this.contentSnapshot = requireNonNull(contentSnapshot);
        this.cardFactory = requireNonNull(cardFactory);
    }

    /** Returns true if this entry represents {@code otherKey}. */
    boolean hasKey(String otherKey) {
        return key.equals(otherKey);
    }

    /** Returns true if this entry would display the same content as {@code other}. */
    boolean hasSameContent(CardListEntry other) {
        return contentSnapshot.equals(other.contentSnapshot);
    }

    /** Returns the stable key used to preserve this record across list updates. */
    String getKey() {
        return key;
    }

    @Override
    public Region get() {
        return cardFactory.get();
    }
}
