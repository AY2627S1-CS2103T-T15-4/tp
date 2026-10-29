package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;

/** Displays customer cards in a list. */
public class CustomerListPanel extends UiPart<Region> {
    private static final String FXML = "RecordListPanel.fxml";

    @FXML
    private ListView<Supplier<Region>> recordListView;

    private final ObservableList<Customer> customers;
    private final ObservableList<Dog> allDogs;

    /** Creates a panel that displays the supplied customers. */
    public CustomerListPanel(ObservableList<Customer> customers, ObservableList<Dog> allDogs) {
        super(FXML);
        this.customers = customers;
        this.allDogs = allDogs;
        recordListView.setPlaceholder(createEmptyState("No customers to display."));
        recordListView.setCellFactory(listView -> new RecordListViewCell());
        customers.addListener((ListChangeListener<Customer>) change -> updateCustomers());
        allDogs.addListener((ListChangeListener<Dog>) change -> updateCustomers());
        updateCustomers();
    }

    /** Reconciles visible customer cards with customer details and current dog ownership. */
    private void updateCustomers() {
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
        CardListUpdater.reconcile(recordListView, cards);
    }

    /** Scrolls to the first customer after an explicit display-changing command. */
    void scrollToTop() {
        CardListUpdater.scrollToTop(recordListView);
    }

    private static Label createEmptyState(String message) {
        Label placeholder = new Label(message);
        placeholder.getStyleClass().add("empty-state");
        return placeholder;
    }

    private static class RecordListViewCell extends ListCell<Supplier<Region>> {
        @Override
        protected void updateItem(Supplier<Region> card, boolean empty) {
            super.updateItem(card, empty);
            setText(null);
            setGraphic(empty || card == null ? null : card.get());
        }
    }
}
