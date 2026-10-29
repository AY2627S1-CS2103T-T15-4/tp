package seedu.address.ui;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
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
    @FXML
    private HBox phoneRow;
    @FXML
    private HBox emailRow;
    @FXML
    private HBox backupContactRow;

    /**
     * Creates a {@code CustomerCard} with the given {@code Customer} to display.
     */
    public CustomerCard(Customer customer) {
        this(customer, List.of());
    }

    /** Creates a customer card with the persistent IDs of all dogs owned by that customer. */
    public CustomerCard(Customer customer, List<Integer> ownedDogIds) {
        this(customer, ownedDogIds, CardInteractionUtil::copyToSystemClipboard);
    }

    /** Creates a customer card that sends copied values to the supplied clipboard writer. */
    CustomerCard(Customer customer, List<Integer> ownedDogIds, Consumer<String> clipboardWriter) {
        super(FXML);
        this.customer = customer;
        id.setText(customer.getId() + ". ");
        name.setText(customer.getName().fullName);
        phone.setText(customer.getPhone().value);
        email.setText(customer.getEmail().value);
        backupContact.setText(customer.getBackupContact().value);
        String ids = ownedDogIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
        dogIds.setText(ids.isEmpty() ? "None" : ids);

        CardInteractionUtil.installFullValueTooltip(phoneRow, customer.getPhone().value);
        CardInteractionUtil.installFullValueTooltip(emailRow, customer.getEmail().value);
        CardInteractionUtil.installFullValueTooltip(backupContactRow, customer.getBackupContact().value);
        installCopyMenu(clipboardWriter, ownedDogIds);
    }

    private void installCopyMenu(Consumer<String> clipboardWriter, List<Integer> ownedDogIds) {
        ContextMenu copyMenu = new ContextMenu(
                CardInteractionUtil.createCopyMenuItem("Copy phone number", customer.getPhone().value,
                        cardPane, clipboardWriter),
                CardInteractionUtil.createCopyMenuItem("Copy email address", customer.getEmail().value,
                        cardPane, clipboardWriter),
                CardInteractionUtil.createCopyMenuItem("Copy backup contact", customer.getBackupContact().value,
                        cardPane, clipboardWriter),
                new SeparatorMenuItem(),
                CardInteractionUtil.createCopyMenuItem("Copy all customer details",
                        CardCopyFormatter.formatCustomer(customer, ownedDogIds), cardPane, clipboardWriter));
        CardInteractionUtil.installCopyMenu(cardPane, copyMenu);
    }
}
