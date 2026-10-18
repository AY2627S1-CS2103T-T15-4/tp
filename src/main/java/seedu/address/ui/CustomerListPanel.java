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
        recordListView.setCellFactory(listView -> new RecordListViewCell());
        customers.addListener((ListChangeListener<Customer>) change -> updateCustomers());
        allDogs.addListener((ListChangeListener<Dog>) change -> updateCustomers());
        updateCustomers();
    }

    private void updateCustomers() {
        List<Supplier<Region>> cards = new ArrayList<>();
        customers.forEach(customer -> cards.add(() -> new CustomerCard(customer, allDogs.stream()
                .filter(dog -> dog.getOwnerId().filter(ownerId -> ownerId == customer.getId()).isPresent())
                .map(Dog::getId).toList()).getRoot()));
        recordListView.getItems().setAll(cards);
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
