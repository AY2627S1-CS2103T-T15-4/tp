package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.dog.Dog;

/**
 * A UI component that displays a dog's persistent ID and details.
 */
public class DogCard extends UiPart<Region> {
    private static final String FXML = "DogListCard.fxml";

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
        breed.setText("Breed: " + dog.getBreed());
        size.setText("Size: " + dog.getSize());
        owner.setText(dog.getOwnerId().map(id -> "Owner ID: " + id).orElse("Owner ID: None"));
    }
}
