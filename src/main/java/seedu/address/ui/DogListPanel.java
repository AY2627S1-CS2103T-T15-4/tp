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
import seedu.address.model.dog.Dog;

/** Displays dog cards in a list. */
public class DogListPanel extends UiPart<Region> {
    private static final String FXML = "RecordListPanel.fxml";

    @FXML
    private ListView<Supplier<Region>> recordListView;

    private final ObservableList<Dog> dogs;

    /** Creates a panel that displays the supplied dogs. */
    public DogListPanel(ObservableList<Dog> dogs) {
        super(FXML);
        this.dogs = dogs;
        recordListView.setCellFactory(listView -> new RecordListViewCell());
        dogs.addListener((ListChangeListener<Dog>) change -> updateDogs());
        updateDogs();
    }

    private void updateDogs() {
        List<Supplier<Region>> cards = new ArrayList<>();
        dogs.forEach(dog -> cards.add(() -> new DogCard(dog).getRoot()));
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
