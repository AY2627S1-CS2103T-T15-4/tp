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

    /**
     * Creates a panel that observes both lists. Cards are created only for visible cells.
     */
    public RecordListPanel(ObservableList<Customer> customers, ObservableList<Dog> dogs) {
        super(FXML);
        this.customers = customers;
        this.dogs = dogs;
        recordListView.setCellFactory(listView -> new RecordListViewCell());
        customers.addListener((ListChangeListener<Customer>) change -> updateRecords());
        dogs.addListener((ListChangeListener<Dog>) change -> updateRecords());
        updateRecords();
    }

    private void updateRecords() {
        List<Supplier<Region>> cards = new ArrayList<>();
        customers.forEach(customer -> cards.add(() -> new CustomerCard(customer).getRoot()));
        dogs.forEach(dog -> cards.add(() -> new DogCard(dog).getRoot()));
        recordListView.getItems().setAll(cards);
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
