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
import seedu.address.model.activity.Activity;

/** Displays activity cards in a list. */
public class ActivityListPanel extends UiPart<Region> {
    private static final String FXML = "RecordListPanel.fxml";

    @FXML
    private ListView<Supplier<Region>> recordListView;

    private final ObservableList<Activity> activities;

    /** Creates a panel that displays the supplied activities. */
    public ActivityListPanel(ObservableList<Activity> activities) {
        super(FXML);
        this.activities = activities;
        recordListView.setCellFactory(listView -> new RecordListViewCell());
        activities.addListener((ListChangeListener<Activity>) change -> updateActivities());
        updateActivities();
    }

    private void updateActivities() {
        List<Supplier<Region>> cards = new ArrayList<>();
        activities.forEach(activity -> cards.add(() -> new ActivityCard(activity).getRoot()));
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
