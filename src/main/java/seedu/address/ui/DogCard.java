package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import seedu.address.model.dog.Dog;

/**
 * A UI component that displays a dog's persistent ID and details.
 */
public class DogCard extends UiPart<Region> {
    private static final String FXML = "DogListCard.fxml";
    private static final String CUSTOMER_ICON = "\uD83D\uDC64";

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

    /**
     * Creates a card with the given dog to display.
     */
    public DogCard(Dog dog) {
        super(FXML);
        id.setText(dog.getId() + ". ");
        name.setText(dog.getName().fullName);
        setSingleLineValue(breed, "Breed: " + dog.getBreed());
        setSingleLineValue(size, "Size: " + dog.getSize());
        setSingleLineValue(owner, CUSTOMER_ICON + " " + dog.getOwnerId().map(String::valueOf).orElse("None"));
    }

    private static void setSingleLineValue(Label label, String value) {
        label.setText(value);
        label.setTooltip(new Tooltip(value));
    }
}
