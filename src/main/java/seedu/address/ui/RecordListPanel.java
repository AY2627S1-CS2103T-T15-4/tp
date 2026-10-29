package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;

/**
 * Displays customers followed by dogs in one scrollable list.
 */
public class RecordListPanel extends UiPart<Region> {
    private static final String FXML = "RecordListPanel.fxml";

    @FXML
    private ListView<Supplier<Region>> recordListView;

    private final ObservableList<Customer> customers;
    private final ObservableList<Dog> dogs;
    private final ObservableList<Dog> allDogs;

    /**
     * Creates a panel that observes both lists. Cards are created only for visible cells.
     */
    public RecordListPanel(ObservableList<Customer> customers, ObservableList<Dog> dogs) {
        this(customers, dogs, dogs);
    }

    /** Observes visible records and all stored dogs so ownership details are independent of filters. */
    public RecordListPanel(ObservableList<Customer> customers, ObservableList<Dog> dogs,
            ObservableList<Dog> allDogs) {
        super(FXML);
        this.customers = customers;
        this.dogs = dogs;
        this.allDogs = allDogs;
        recordListView.setCellFactory(listView -> new RecordListViewCell());
        customers.addListener((ListChangeListener<Customer>) change -> updateRecords());
        dogs.addListener((ListChangeListener<Dog>) change -> updateRecords());
        if (allDogs != dogs) {
            allDogs.addListener((ListChangeListener<Dog>) change -> updateRecords());
        }
        updateRecords();
    }

    /** Reconciles combined customer and dog cards while retaining stable record identity. */
    private void updateRecords() {
        List<CardListEntry> cards = new ArrayList<>();
        customers.forEach(customer -> {
            List<Integer> ownedDogIds = allDogs.stream()
                .filter(dog -> dog.getOwnerId().filter(ownerId -> ownerId == customer.getId()).isPresent())
                .map(Dog::getId)
                .sorted()
                .toList();
            Supplier<Region> cardFactory = () -> new CustomerCard(customer, ownedDogIds).getRoot();
            cards.add(new CardListEntry("customer:" + customer.getId(), List.of(customer, ownedDogIds), cardFactory));
        });
        dogs.forEach(dog -> {
            Supplier<Region> cardFactory = () -> new DogCard(dog).getRoot();
            cards.add(new CardListEntry("dog:" + dog.getId(), dog, cardFactory));
        });
        CardListUpdater.reconcile(recordListView, cards);
    }

    /** Scrolls to the first record after an explicit display-changing command. */
    void scrollToTop() {
        CardListUpdater.scrollToTop(recordListView);
    }

    /**
     * Renders either kind of card using the existing list-cell styling.
     */
    private static class RecordListViewCell extends ListCell<Supplier<Region>> {
        @Override
        protected void updateItem(Supplier<Region> card, boolean empty) {
            super.updateItem(card, empty);
            setText(null);
            setGraphic(empty || card == null ? null : card.get());
        }
    }
}
