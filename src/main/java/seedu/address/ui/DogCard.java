package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
// @@author Kenneth-Chia
import javafx.scene.layout.Region;
import seedu.address.model.dog.Dog;

/**
 * A UI component that displays a dog's persistent ID and details.
 */
public class DogCard extends UiPart<Region> {
    private static final String FXML = "DogListCard.fxml";
    // @@author
    private static final String CUSTOMER_ICON = "\uD83D\uDC64";

    @FXML
    private HBox cardPane;
    // @@author Kenneth-Chia
    @FXML
    private Label id;
    @FXML
    private Label name;
    @FXML
    private Label breed;
    @FXML
    private Label size;
    @FXML
    private Label owner;
    // @@author
    @FXML
    private HBox breedRow;
    @FXML
    private HBox sizeRow;
    @FXML
    private HBox ownerRow;

    // @@author Kenneth-Chia
    /**
     * Creates a card with the given dog to display.
     */
    public DogCard(Dog dog) {
        // @@author
        this(dog, CardInteractionUtil::copyToSystemClipboard);
    }

    /** Creates a dog card that sends copied values to the supplied clipboard writer. */
    DogCard(Dog dog, ClipboardWriter clipboardWriter) {
        // @@author Kenneth-Chia
        super(FXML);
        id.setText(dog.getId() + ". ");
        name.setText(dog.getName().fullName);
        // @@author
        String breedText = "Breed: " + dog.getBreed();
        String sizeText = "Size: " + dog.getSize();
        String customerId = dog.getOwnerId().map(String::valueOf).orElse("None");
        breed.setText(breedText);
        size.setText(sizeText);
        owner.setText(CUSTOMER_ICON + " " + customerId);

        CardInteractionUtil.installFullValueTooltip(breedRow, breedText);
        CardInteractionUtil.installFullValueTooltip(sizeRow, sizeText);
        CardInteractionUtil.installFullValueTooltip(ownerRow, "Customer ID: " + customerId);
        installCopyMenu(dog, clipboardWriter);
    }

    /** Installs actions for copying dog details that are available on this card. */
    private void installCopyMenu(Dog dog, ClipboardWriter clipboardWriter) {
        MenuItem copyCustomerId = dog.getOwnerId()
                .map(ownerId -> CardInteractionUtil.createCopyMenuItem("Copy customer ID",
                        String.valueOf(ownerId), cardPane, clipboardWriter))
                .orElseGet(() -> {
                    MenuItem disabledItem = new MenuItem("Copy customer ID");
                    disabledItem.setDisable(true);
                    return disabledItem;
                });
        ContextMenu copyMenu = new ContextMenu(
                CardInteractionUtil.createCopyMenuItem("Copy breed", dog.getBreed().toString(),
                        cardPane, clipboardWriter),
                copyCustomerId,
                new SeparatorMenuItem(),
                CardInteractionUtil.createCopyMenuItem("Copy all dog details", CardCopyFormatter.formatDog(dog),
                        cardPane, clipboardWriter));
        CardInteractionUtil.installCopyMenu(cardPane, copyMenu);
    }
}
