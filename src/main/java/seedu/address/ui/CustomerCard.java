package seedu.address.ui;

import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.customer.Customer;

/**
 * A UI component that displays information of a {@code Customer}.
 */
public class CustomerCard extends UiPart<Region> {

    private static final String FXML = "CustomerListCard.fxml";

    public final Customer customer;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label backupContact;
    @FXML
    private Label dogIds;

    /**
     * Creates a {@code CustomerCard} with the given {@code Customer} to display.
     */
    public CustomerCard(Customer customer) {
        this(customer, List.of());
    }

    /** Creates a customer card with the persistent IDs of all dogs owned by that customer. */
    public CustomerCard(Customer customer, List<Integer> ownedDogIds) {
        super(FXML);
        this.customer = customer;
        id.setText(customer.getId() + ". ");
        name.setText(customer.getName().fullName);
        phone.setText(customer.getPhone().value);
        email.setText(customer.getEmail().value);
        backupContact.setText(customer.getBackupContact().value);
        String ids = ownedDogIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
        dogIds.setText("Dog IDs: " + (ids.isEmpty() ? "None" : ids));
    }
}
