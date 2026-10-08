package seedu.address.ui;

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

    /**
     * Creates a {@code CustomerCard} with the given {@code Customer} to display.
     */
    public CustomerCard(Customer customer) {
        super(FXML);
        this.customer = customer;
        id.setText(customer.getId() + ". ");
        name.setText(customer.getName());
        phone.setText(customer.getPhone());
        email.setText(customer.getEmail());
        backupContact.setText(customer.getBackupContact());
    }
}
